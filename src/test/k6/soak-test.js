import { check, sleep } from 'k6';
import http from 'k6/http';
import {
  BASE_URL,
  HEADERS,
  randomTitle,
  checkStatus,
  createBook,
  createMember,
  borrowBook,
  returnBook,
} from './config.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },   // ramp-up to 50 VUs
    { duration: '13m', target: 50 },   // hold 50 VUs steady
    { duration: '1m', target: 0 },     // ramp-down to 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.01'],
    checks: ['rate==1.0'],
  },
};

export default function () {
  // Step 1: Add a book
  const { res: bookRes, body: book } = createBook(randomTitle(), 'Soak Author', 3);
  checkStatus(bookRes, 200, 'Soak - Add book');

  // Step 2: Add a member
  const { res: memberRes, body: member } = createMember();
  checkStatus(memberRes, 200, 'Soak - Add member');

  // Step 3: Borrow the book
  const { res: borrowRes, body: borrow } = borrowBook(member.id, book.id);
  checkStatus(borrowRes, 200, 'Soak - Borrow book');
  check(borrowRes, {
    'Soak - borrow returnedAt is null': () => borrow.returnedAt === null,
  });

  // Step 4: Verify available copies decremented
  const afterBorrowRes = http.get(`${BASE_URL}/books/getBook/${book.id}`, HEADERS);
  checkStatus(afterBorrowRes, 200, 'Soak - Get book after borrow');
  check(afterBorrowRes, {
    'Soak - availableCopies decremented': (r) => r.json().availableCopies === 2,
  });

  // Step 5: Return the book
  const { res: retRes, body: returned } = returnBook(borrow.id);
  checkStatus(retRes, 200, 'Soak - Return book');
  check(retRes, {
    'Soak - returnedAt is set': () => returned.returnedAt !== null,
  });

  // Step 6: Verify copies restored
  const afterReturnRes = http.get(`${BASE_URL}/books/getBook/${book.id}`, HEADERS);
  checkStatus(afterReturnRes, 200, 'Soak - Get book after return');
  check(afterReturnRes, {
    'Soak - availableCopies restored': (r) => r.json().availableCopies === 3,
  });

  // Realistic pacing between iterations
  sleep(1);
}
