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
} from './config.js';

export const options = OPTIONS;

export default function () {
  // ==================== HAPPY PATH TESTS ====================

  // --- POST /books/addBooks ---
  const title = randomTitle();
  const { res: addRes, body: addedBook } = createBook(title, 'Jane Austen', 5);
  checkStatus(addRes, 200, 'POST /addBooks');
  check(addRes, {
    'POST /addBooks - has id': () => addedBook.id !== undefined,
    'POST /addBooks - title matches': () => addedBook.title === title,
    'POST /addBooks - author matches': () => addedBook.author === 'Jane Austen',
    'POST /addBooks - totalCopies is 5': () => addedBook.totalCopies === 5,
    'POST /addBooks - availableCopies equals totalCopies': () =>
      addedBook.availableCopies === addedBook.totalCopies,
  });

  const bookId = addedBook.id;

  // --- GET /books/getAllBooks ---
  const getAllRes = http.get(`${BASE_URL}/books/getAllBooks`, HEADERS);
  checkStatus(getAllRes, 200, 'GET /getAllBooks');
  checkIsArray(getAllRes, 'GET /getAllBooks');
  check(getAllRes, {
    'GET /getAllBooks - contains at least 1 book': (r) => r.json().length >= 1,
  });

  // --- GET /books/getAvailableBooks ---
  const availRes = http.get(`${BASE_URL}/books/getAvailableBooks`, HEADERS);
  checkStatus(availRes, 200, 'GET /getAvailableBooks');
  checkIsArray(availRes, 'GET /getAvailableBooks');
  check(availRes, {
    'GET /getAvailableBooks - all have availableCopies > 0': (r) =>
      r.json().every((b) => b.availableCopies > 0),
  });

  // --- GET /books/getBook/{id} ---
  const getOneRes = http.get(`${BASE_URL}/books/getBook/${bookId}`, HEADERS);
  checkStatus(getOneRes, 200, 'GET /getBook/{id}');
  check(getOneRes, {
    'GET /getBook/{id} - id matches': (r) => r.json().id === bookId,
    'GET /getBook/{id} - title matches': (r) => r.json().title === title,
  });

  // --- PUT /books/updateCopies/{id} ---
  const updatePayload = JSON.stringify({ totalCopies: 10 });
  const updateRes = http.put(
    `${BASE_URL}/books/updateCopies/${bookId}`,
    updatePayload,
    HEADERS
  );
  checkStatus(updateRes, 200, 'PUT /updateCopies');
  check(updateRes, {
    'PUT /updateCopies - totalCopies updated to 10': (r) => r.json().totalCopies === 10,
    'PUT /updateCopies - availableCopies updated to 10': (r) => r.json().availableCopies === 10,
  });

  // ==================== NEGATIVE / ERROR TESTS ====================

  // --- GET /books/getBook/{id} with non-existent ID (expect 404) ---
  const notFoundRes = http.get(`${BASE_URL}/books/getBook/999999`, HEADERS);
  checkErrorResponse(notFoundRes, 404, 'GET /getBook/999999 (not found)');

  // --- POST /books/addBooks with blank title (expect 400) ---
  const blankTitlePayload = JSON.stringify({
    title: '',
    author: 'Author',
    totalCopies: 1,
  });
  const blankTitleRes = http.post(
    `${BASE_URL}/books/addBooks`,
    blankTitlePayload,
    HEADERS
  );
  checkErrorResponse(blankTitleRes, 400, 'POST /addBooks blank title');

  // --- POST /books/addBooks with totalCopies = 0 (expect 400) ---
  const zeroCopiesPayload = JSON.stringify({
    title: 'Valid Title',
    author: 'Author',
    totalCopies: 0,
  });
  const zeroCopiesRes = http.post(
    `${BASE_URL}/books/addBooks`,
    zeroCopiesPayload,
    HEADERS
  );
  checkErrorResponse(zeroCopiesRes, 400, 'POST /addBooks totalCopies=0');

  // --- PUT /books/updateCopies reducing below lent copies (expect 400) ---
  // First, create a book with 1 copy and borrow it
  const { body: singleCopyBook } = createBook(randomTitle(), 'Author', 1);
  const { body: tempMember } = createMember();
  borrowBook(tempMember.id, singleCopyBook.id);

  // Now try to set totalCopies to 0 (1 is lent out) -> should fail
  const invalidUpdatePayload = JSON.stringify({ totalCopies: 0 });
  const invalidUpdateRes = http.put(
    `${BASE_URL}/books/updateCopies/${singleCopyBook.id}`,
    invalidUpdatePayload,
    HEADERS
  );
  checkErrorResponse(invalidUpdateRes, 400, 'PUT /updateCopies below lent copies');
}
