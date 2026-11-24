import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 5,
  duration: __ENV.DURATION ? __ENV.DURATION : '10s',
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const credentials = 'admin:password';
const encodedAuth = 'Basic ' + Buffer.from(credentials).toString('base64');

export default function () {
  const uniqueKey = 'K6-IDEM-' + Date.now() + '-' + Math.floor(Math.random() * 1000000);
  const payload = JSON.stringify({
    idempotencyKey: uniqueKey,
    amount: 1000.00,
    paymentMethod: 'SIMULATED_GATEWAY',
    notes: 'k6 automated load test payment',
  });

  const res = http.post(`${BASE_URL}/api/loans/1/repayments`, payload, {
    headers: {
      Authorization: encodedAuth,
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'payment confirmed': (r) => JSON.parse(r.body).data.status === 'SUCCESS',
  });

  sleep(0.2);
}
