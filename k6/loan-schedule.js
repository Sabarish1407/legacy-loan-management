import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
  duration: __ENV.DURATION ? __ENV.DURATION : '10s',
  thresholds: {
    http_req_duration: ['p(95)<400'],
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const credentials = 'admin:password';
const encodedAuth = 'Basic ' + Buffer.from(credentials).toString('base64');

export default function () {
  const res = http.get(`${BASE_URL}/api/loans/1/schedule`, {
    headers: {
      Authorization: encodedAuth,
      Accept: 'application/json',
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'schedule retrieved': (r) => JSON.parse(r.body).data.schedules !== undefined,
  });

  sleep(0.1);
}
