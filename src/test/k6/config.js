import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = 'http://localhost:8080/api/v1';

// ===== Shared K6 options — change VUs and iterations here =====
export const OPTIONS = {
  vus: 50,
  iterations: 50,
  thresholds: {
    checks: ['rate>=0.95'],
  },
};

export const HEADERS = {
  headers: { 'Content-Type': 'application/json' },
};

// Generate a random suffix to avoid uniqueness conflicts on re-runs
export function randomSuffix() {
  return `${Date.now()}_${Math.random().toString(36).substring(2, 8)}`;
}

// Generate a unique email address
export function randomEmail() {
  return `testuser_${randomSuffix()}@test.com`;
}

// Generate a unique book title
export function randomTitle() {
  return `Test Book ${randomSuffix()}`;
}

// Reusable check: verify response status and optionally check body fields
export function checkStatus(res, expectedStatus, label) {
  check(res, {
    [`${label} - status is ${expectedStatus}`]: (r) => r.status === expectedStatus,
  });
}

// Reusable check: verify response is a JSON array
export function checkIsArray(res, label) {
  check(res, {
    [`${label} - response is an array`]: (r) => Array.isArray(r.json()),
  });
}

// Reusable check: verify error response structure (status, error, message)
export function checkErrorResponse(res, expectedStatus, label) {
  check(res, {
    [`${label} - status is ${expectedStatus}`]: (r) => r.status === expectedStatus,
    [`${label} - has error field`]: (r) => r.json().error !== undefined,
    [`${label} - has message field`]: (r) => r.json().message !== undefined,
    [`${label} - has status field`]: (r) => r.json().status !== undefined,
  });
}

// Helper: create a book via API and return the parsed response body
export function createBook(title, author, totalCopies) {
  const payload = JSON.stringify({
    title: title || randomTitle(),
    author: author || 'Test Author',
    totalCopies: totalCopies || 3,
  });
  const res = http.post(`${BASE_URL}/books/addBooks`, payload, HEADERS);
  return { res, body: res.json() };
}

// Helper: create a member via API and return the parsed response body
export function createMember(name, email) {
  const payload = JSON.stringify({
    name: name || 'Test Member',
    email: email || randomEmail(),
  });
  const res = http.post(`${BASE_URL}/members/addMember`, payload, HEADERS);
  return { res, body: res.json() };
}

// Helper: borrow a book via API and return the parsed response body
export function borrowBook(memberId, bookId) {
  const payload = JSON.stringify({ memberId, bookId });
  const res = http.post(`${BASE_URL}/borrows/borrowBook`, payload, HEADERS);
  return { res, body: res.json() };
}

// Helper: return a book via API and return the parsed response body
export function returnBook(borrowId) {
  const res = http.put(`${BASE_URL}/borrows/returnBook/${borrowId}`, null, HEADERS);
  return { res, body: res.json() };
}
