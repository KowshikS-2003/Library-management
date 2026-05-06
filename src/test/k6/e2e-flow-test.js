import { check } from 'k6';
import http from 'k6/http';
import {
  BASE_URL,
  HEADERS,
  OPTIONS,
  randomTitle,
  checkStatus,
  createBook,
  createMember,
  borrowBook,
  returnBook,
} from './config.js';

export const options = OPTIONS;

export default function () {
  // ==================== STEP 1: Add a book ====================
  const title = randomTitle();
  const author = 'E2E Author';
  const totalCopies = 3;

  const { res: bookRes, body: book } = createBook(title, author, totalCopies);
  checkStatus(bookRes, 200, 'E2E Step 1 - Add book');
  check(bookRes, {
    'E2E Step 1 - book created with correct copies': () =>
      book.totalCopies === totalCopies && book.availableCopies === totalCopies,
  });

  // ==================== STEP 2: Add a member ====================
  const { res: memberRes, body: member } = createMember();
  checkStatus(memberRes, 200, 'E2E Step 2 - Add member');
  check(memberRes, {
    'E2E Step 2 - member has id and createdAt': () =>
      member.id !== undefined && member.createdAt !== undefined,
  });

  // ==================== STEP 3: Borrow the book ====================
  const { res: borrowRes, body: borrow } = borrowBook(member.id, book.id);
  checkStatus(borrowRes, 200, 'E2E Step 3 - Borrow book');
  check(borrowRes, {
    'E2E Step 3 - borrow links correct member and book': () =>
      borrow.memberId === member.id && borrow.bookId === book.id,
    'E2E Step 3 - returnedAt is null (active borrow)': () =>
      borrow.returnedAt === null,
  });

  // ==================== STEP 4: Verify available copies decremented ====================
  const afterBorrowRes = http.get(`${BASE_URL}/books/getBook/${book.id}`, HEADERS);
  checkStatus(afterBorrowRes, 200, 'E2E Step 4 - Get book after borrow');
  check(afterBorrowRes, {
    'E2E Step 4 - availableCopies decremented by 1': (r) =>
      r.json().availableCopies === totalCopies - 1,
    'E2E Step 4 - totalCopies unchanged': (r) =>
      r.json().totalCopies === totalCopies,
  });

  // ==================== STEP 5: Return the book ====================
  const { res: retRes, body: returned } = returnBook(borrow.id);
  checkStatus(retRes, 200, 'E2E Step 5 - Return book');
  check(retRes, {
    'E2E Step 5 - returnedAt is set': () => returned.returnedAt !== null,
  });

  // ==================== STEP 6: Verify available copies restored ====================
  const afterReturnRes = http.get(`${BASE_URL}/books/getBook/${book.id}`, HEADERS);
  checkStatus(afterReturnRes, 200, 'E2E Step 6 - Get book after return');
  check(afterReturnRes, {
    'E2E Step 6 - availableCopies restored': (r) =>
      r.json().availableCopies === totalCopies,
    'E2E Step 6 - totalCopies still unchanged': (r) =>
      r.json().totalCopies === totalCopies,
  });

  // ==================== STEP 7: Verify borrow history ====================
  const historyRes = http.get(
    `${BASE_URL}/borrows/getBorrowHistory/${member.id}`,
    HEADERS
  );
  checkStatus(historyRes, 200, 'E2E Step 7 - Get borrow history');
  check(historyRes, {
    'E2E Step 7 - history has 1 record': (r) => r.json().length === 1,
    'E2E Step 7 - record has returnedAt set': (r) =>
      r.json()[0].returnedAt !== null,
    'E2E Step 7 - record matches correct book': (r) =>
      r.json()[0].bookId === book.id,
    'E2E Step 7 - record matches correct member': (r) =>
      r.json()[0].memberId === member.id,
  });
}
