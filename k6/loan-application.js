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
const credentials = 'officer:password';
const encodedAuth = 'Basic ' + Buffer.from(credentials).toString('base64');

export default function () {
  const payload = JSON.stringify({
    customerId: 1,
    loanType: 'PERSONAL',
    requestedAmount: 50000.00,
    interestRate: 10.00,
    tenureMonths: 12,
    employmentType: 'SALARIED',
    monthlyIncome: 75000.00,
  });

  const res = http.post(`${BASE_URL}/api/loans/applications`, payload, {
    headers: {
      Authorization: encodedAuth,
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
  });

  check(res, {
    'status is 201': (r) => r.status === 201,
    'application created': (r) => JSON.parse(r.body).data.id !== undefined,
  });

  sleep(0.2);
}
