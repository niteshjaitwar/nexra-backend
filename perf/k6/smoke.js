import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<800'],
  },
};

const baseUrl = __ENV.NEXRA_BASE_URL || 'http://localhost:8081';

export default function () {
  const health = http.get(`${baseUrl}/actuator/health`);
  check(health, {
    'health is 200': (res) => res.status === 200,
  });
  sleep(1);
}
