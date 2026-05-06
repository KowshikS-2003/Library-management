import { check } from 'k6';
import http from 'k6/http';
import {
  BASE_URL,
  HEADERS,
  OPTIONS,
  randomTitle,
  checkStatus,
  checkIsArray,
  checkErrorResponse,
  createBook,
  createMember,
  borrowBook,
  returnBook,
} from './config.js';

export const options = OPTIONS;

export default function () {
  // ==================== SETUP: Create test book and member ====================
  const { body: book } = createBook(randomTitle(), 'Test Author', 5);
  const { body: member } = createMember();

  // ==================== HAPPY PATH TESTS ====================

  // --- POST /borrows/borrowBook ---
  const { res: borrowRes, body: borrowRecord } = borrowBook(member.id, book.id);
  checkStatus(borrowRes, 200, 'POST /borrowBook');
  check(borrowRes, {
    'POST /borrowBook - has id': () => borrowRecord.id !== undefined,
    'POST /borrowBook - memberId matches': () => borrowRecord.memberId === member.id,
    'POST /borrowBook - memberName matches': () => borrowRecord.memberName === member.name,
    'POST /borrowBook - bookId matches': () => borrowRecord.bookId === book.id,
    'POST /borrowBook - bookTitle matches': () => borrowRecord.bookTitle === book.title,
    'POST /borrowBook - has borrowedAt': () => borrowRecord.borrowedAt !== undefined,
    'POST /borrowBook - returnedAt is null': () => borrowRecord.returnedAt === null,
  });

  const borrowId = borrowRecord.id;

  // --- GET /borrows/getAllBorrows ---
  const getAllRes = http.get(`${BASE_URL}/borrows/getAllBorrows`, HEADERS);
  checkStatus(getAllRes, 200, 'GET /getAllBorrows');
  checkIsArray(getAllRes, 'GET /getAllBorrows');
  check(getAllRes, {
    'GET /getAllBorrows - contains at least 1 record': (r) => r.json().length >= 1,
  });

  // --- GET /borrows/getBorrowHistory/{memberId} ---
  const historyRes = http.get(
    `${BASE_URL}/borrows/getBorrowHistory/${member.id}`,
    HEADERS
  );
  checkStatus(historyRes, 200, 'GET /getBorrowHistory/{memberId}');
  checkIsArray(historyRes, 'GET /getBorrowHistory/{memberId}');
  check(historyRes, {
    'GET /getBorrowHistory - contains at least 1 record': (r) => r.json().length >= 1,
    'GET /getBorrowHistory - records belong to correct member': (r) =>
      r.json().every((rec) => rec.memberId === member.id),
  });

  // --- PUT /borrows/returnBook/{id} ---
  const { res: returnRes, body: returnedRecord } = returnBook(borrowId);
  checkStatus(returnRes, 200, 'PUT /returnBook/{id}');
  check(returnRes, {
    'PUT /returnBook - returnedAt is not null': () => returnedRecord.returnedAt !== null,
    'PUT /returnBook - id matches': () => returnedRecord.id === borrowId,
  });

  // ==================== NEGATIVE / ERROR TESTS ====================

  // --- POST /borrows/borrowBook with non-existent member (expect 404) ---
  const noMemberPayload = JSON.stringify({ memberId: 999999, bookId: book.id });
  const noMemberRes = http.post(
    `${BASE_URL}/borrows/borrowBook`,
    noMemberPayload,
    HEADERS
  );
  checkErrorResponse(noMemberRes, 404, 'POST /borrowBook non-existent member');

  // --- POST /borrows/borrowBook with non-existent book (expect 404) ---
  const noBookPayload = JSON.stringify({ memberId: member.id, bookId: 999999 });
  const noBookRes = http.post(
    `${BASE_URL}/borrows/borrowBook`,
    noBookPayload,
    HEADERS
  );
  checkErrorResponse(noBookRes, 404, 'POST /borrowBook non-existent book');

  // --- Borrow limit exceeded: borrow 3 books, then attempt 4th (expect 409) ---
  const { body: limitMember } = createMember();
  const bookIds = [];
  for (let i = 0; i < 4; i++) {
    const { body: b } = createBook(randomTitle(), 'Author', 1);
    bookIds.push(b.id);
  }
  // Borrow 3 books successfully
  for (let i = 0; i < 3; i++) {
    borrowBook(limitMember.id, bookIds[i]);
  }
  // 4th borrow should fail with 409
  const limitRes = http.post(
    `${BASE_URL}/borrows/borrowBook`,
    JSON.stringify({ memberId: limitMember.id, bookId: bookIds[3] }),
    HEADERS
  );
  checkErrorResponse(limitRes, 409, 'POST /borrowBook borrow limit exceeded');

  // --- Book not available: borrow all copies, then attempt another (expect 409) ---
  const { body: singleBook } = createBook(randomTitle(), 'Author', 1);
  const { body: borrower1 } = createMember();
  const { body: borrower2 } = createMember();
  borrowBook(borrower1.id, singleBook.id); // takes the only copy
  const noAvailRes = http.post(
    `${BASE_URL}/borrows/borrowBook`,
    JSON.stringify({ memberId: borrower2.id, bookId: singleBook.id }),
    HEADERS
  );
  checkErrorResponse(noAvailRes, 409, 'POST /borrowBook book not available');

  // --- PUT /borrows/returnBook on already-returned record (expect 409) ---
  const alreadyReturnedRes = http.put(
    `${BASE_URL}/borrows/returnBook/${borrowId}`,
    null,
    HEADERS
  );
  checkErrorResponse(alreadyReturnedRes, 409, 'PUT /returnBook already returned');
}
