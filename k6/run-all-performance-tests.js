import http from 'k6/http';
import { check, group, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '3s', target: 5 },  // Ramp up
    { duration: '10s', target: 10 }, // Sustained load
    { duration: '2s', target: 0 },  // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<600'],
    http_req_failed: ['rate<0.05'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const credentials = 'admin:password';
const encodedAuth = 'Basic ' + Buffer.from(credentials).toString('base64');

export default function () {
  group('Customer Operations', function () {
    const res = http.get(`${BASE_URL}/api/customers/1`, {
      headers: { Authorization: encodedAuth },
    });
    check(res, { 'customer lookup 200': (r) => r.status === 200 });
  });

  group('Loan Schedules', function () {
    const res = http.get(`${BASE_URL}/api/loans/1/schedule`, {
      headers: { Authorization: encodedAuth },
    });
    check(res, { 'schedule lookup 200': (r) => r.status === 200 });
  });

  group('Eligibility Assessment', function () {
    const payload = JSON.stringify({
      loanType: 'PERSONAL',
      requestedAmount: 50000.00,
      tenureMonths: 12,
      monthlyIncome: 75000.00,
      employmentType: 'SALARIED',
      creditScore: 750,
    });
    const res = http.post(`${BASE_URL}/api/loans/eligibility`, payload, {
      headers: {
        Authorization: encodedAuth,
        'Content-Type': 'application/json',
      },
    });
    check(res, { 'eligibility 200': (r) => r.status === 200 });
  });

  sleep(0.2);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    'k6/summary.json': JSON.stringify(data, null, 2),
  };
}

function textSummary(data, options) {
  let summary = '\n=== K6 PERFORMANCE BENCHMARK SUMMARY ===\n';
  summary += `Total Requests: ${data.metrics.http_reqs.values.count}\n`;
  summary += `Reqs/sec: ${data.metrics.http_reqs.values.rate.toFixed(2)}\n`;
  summary += `Average Latency: ${data.metrics.http_req_duration.values.avg.toFixed(2)} ms\n`;
  summary += `P95 Latency: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)} ms\n`;
  summary += `Failure Rate: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)} %\n`;
  summary += '========================================\n';
  return summary;
}
