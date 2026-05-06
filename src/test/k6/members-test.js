import { check } from 'k6';
import http from 'k6/http';
import {
  BASE_URL,
  HEADERS,
  OPTIONS,
  randomEmail,
  checkStatus,
  checkIsArray,
  checkErrorResponse,
  createMember,
} from './config.js';

export const options = OPTIONS;

export default function () {
  // ==================== HAPPY PATH TESTS ====================

  // --- POST /members/addMember ---
  const name = 'Alice Johnson';
  const email = randomEmail();
  const { res: addRes, body: addedMember } = createMember(name, email);
  checkStatus(addRes, 200, 'POST /addMember');
  check(addRes, {
    'POST /addMember - has id': () => addedMember.id !== undefined,
    'POST /addMember - name matches': () => addedMember.name === name,
    'POST /addMember - email matches': () => addedMember.email === email,
    'POST /addMember - has createdAt': () => addedMember.createdAt !== undefined,
  });

  const memberId = addedMember.id;

  // --- GET /members/getAllMembers ---
  const getAllRes = http.get(`${BASE_URL}/members/getAllMembers`, HEADERS);
  checkStatus(getAllRes, 200, 'GET /getAllMembers');
  checkIsArray(getAllRes, 'GET /getAllMembers');
  check(getAllRes, {
    'GET /getAllMembers - contains at least 1 member': (r) => r.json().length >= 1,
  });

  // --- GET /members/getMember/{id} ---
  const getOneRes = http.get(`${BASE_URL}/members/getMember/${memberId}`, HEADERS);
  checkStatus(getOneRes, 200, 'GET /getMember/{id}');
  check(getOneRes, {
    'GET /getMember/{id} - id matches': (r) => r.json().id === memberId,
    'GET /getMember/{id} - name matches': (r) => r.json().name === name,
    'GET /getMember/{id} - email matches': (r) => r.json().email === email,
    'GET /getMember/{id} - has createdAt': (r) => r.json().createdAt !== undefined,
  });

  // ==================== NEGATIVE / ERROR TESTS ====================

  // --- GET /members/getMember/{id} with non-existent ID (expect 404) ---
  const notFoundRes = http.get(`${BASE_URL}/members/getMember/999999`, HEADERS);
  checkErrorResponse(notFoundRes, 404, 'GET /getMember/999999 (not found)');

  // --- POST /members/addMember with blank name (expect 400) ---
  const blankNamePayload = JSON.stringify({
    name: '',
    email: randomEmail(),
  });
  const blankNameRes = http.post(
    `${BASE_URL}/members/addMember`,
    blankNamePayload,
    HEADERS
  );
  checkErrorResponse(blankNameRes, 400, 'POST /addMember blank name');

  // --- POST /members/addMember with invalid email (expect 400) ---
  const invalidEmailPayload = JSON.stringify({
    name: 'Valid Name',
    email: 'not-an-email',
  });
  const invalidEmailRes = http.post(
    `${BASE_URL}/members/addMember`,
    invalidEmailPayload,
    HEADERS
  );
  checkErrorResponse(invalidEmailRes, 400, 'POST /addMember invalid email');
}
