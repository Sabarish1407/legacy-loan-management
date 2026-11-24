import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
  duration: __ENV.DURATION ? __ENV.DURATION : '10s',
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.01'],    // less than 1% errors
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const credentials = 'admin:password';
const encodedAuth = 'Basic ' + Buffer.from(credentials).toString('base64');

export default function () {
  const customerId = Math.floor(Math.random() * 3) + 1;
  const res = http.get(`${BASE_URL}/api/customers/${customerId}`, {
    headers: {
      Authorization: encodedAuth,
      Accept: 'application/json',
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'has customer data': (r) => JSON.parse(r.body).data !== undefined,
  });

  sleep(0.1);
}
