package com.neura.loanmanagement.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LoanManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("HTTP Integration Tests for Error Handling and Ordered Loan Lifecycle")
class LoanApiHttpIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @DisplayName("Malformed JSON returns 400 Bad Request instead of 500")
    void testMalformedJsonReturns400() {
        HttpEntity<String> request = new HttpEntity<>("{ invalid json", jsonHeaders());
        ResponseEntity<String> response = restTemplate.withBasicAuth("admin", "password")
                .postForEntity("/api/customers", request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Invalid enum value returns 400 Bad Request instead of 500")
    void testInvalidEnumReturns400() {
        String payload = "{"
                + "\"firstName\":\"Test\","
                + "\"lastName\":\"User\","
                + "\"email\":\"enum.test@example.com\","
                + "\"phone\":\"+1-555-9001\","
                + "\"nationalId\":\"NAT-ENUM-001\","
                + "\"address\":\"100 Test St\","
                + "\"dateOfBirth\":\"1990-01-01\","
                + "\"monthlyIncome\":60000.00,"
                + "\"employmentType\":\"INVALID_EMPLOYMENT\","
                + "\"creditScore\":720"
                + "}";
        HttpEntity<String> request = new HttpEntity<>(payload, jsonHeaders());
        ResponseEntity<String> response = restTemplate.withBasicAuth("admin", "password")
                .postForEntity("/api/customers", request, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Wrong Content-Type returns 415 Unsupported Media Type instead of 500")
    void testWrongContentTypeReturns415() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> request = new HttpEntity<>("plain text body", headers);
        ResponseEntity<String> response = restTemplate.withBasicAuth("admin", "password")
                .postForEntity("/api/customers", request, String.class);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
    }

    @Test
    @DisplayName("Invalid path parameter type (/api/customers/abc) returns 400 Bad Request instead of 500")
    void testTypeMismatchReturns400() {
        ResponseEntity<String> response = restTemplate.withBasicAuth("admin", "password")
                .getForEntity("/api/customers/abc", String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Ordered Loan Lifecycle over HTTP: create customer -> submit -> approve -> disburse -> repay")
    void testOrderedDisburseAndRepayOverHttp() throws Exception {
        // 1. Create customer
        String customerPayload = "{"
                + "\"firstName\":\"Http\","
                + "\"lastName\":\"Tester\","
                + "\"email\":\"http.tester@example.com\","
                + "\"phone\":\"+1-555-9002\","
                + "\"nationalId\":\"NAT-HTTP-002\","
                + "\"address\":\"200 HTTP Ave\","
                + "\"dateOfBirth\":\"1991-03-15\","
                + "\"monthlyIncome\":80000.00,"
                + "\"employmentType\":\"SALARIED\","
                + "\"creditScore\":760"
                + "}";
        ResponseEntity<String> custResp = restTemplate.withBasicAuth("admin", "password")
                .postForEntity("/api/customers", new HttpEntity<>(customerPayload, jsonHeaders()), String.class);
        assertEquals(HttpStatus.CREATED, custResp.getStatusCode());
        long customerId = objectMapper.readTree(custResp.getBody()).get("data").get("id").asLong();

        // 2. Submit loan application
        String appPayload = String.format(
                "{\"customerId\":%d,\"loanType\":\"PERSONAL\",\"requestedAmount\":40000.00,\"interestRate\":10.00,\"tenureMonths\":12,\"employmentType\":\"SALARIED\",\"monthlyIncome\":80000.00}",
                customerId);
        ResponseEntity<String> appResp = restTemplate.withBasicAuth("officer", "password")
                .postForEntity("/api/loans/applications", new HttpEntity<>(appPayload, jsonHeaders()), String.class);
        assertEquals(HttpStatus.CREATED, appResp.getStatusCode());
        long applicationId = objectMapper.readTree(appResp.getBody()).get("data").get("id").asLong();

        // 3. Approve loan application
        String approvePayload = "{\"approvedAmount\":40000.00,\"approvedTenureMonths\":12,\"approvedInterestRate\":10.00,\"reviewNotes\":\"Approved in HTTP IT\"}";
        ResponseEntity<String> approveResp = restTemplate.withBasicAuth("officer", "password")
                .exchange("/api/loans/applications/" + applicationId + "/approve", HttpMethod.PATCH,
                        new HttpEntity<>(approvePayload, jsonHeaders()), String.class);
        assertEquals(HttpStatus.OK, approveResp.getStatusCode());

        // 4. Disburse approved loan
        String disbursePayload = "{\"amount\":40000.00,\"accountNumber\":\"LN-HTTP-IT-001\"}";
        ResponseEntity<String> disburseResp = restTemplate.withBasicAuth("officer", "password")
                .postForEntity("/api/loans/" + applicationId + "/disburse",
                        new HttpEntity<>(disbursePayload, jsonHeaders()), String.class);
        assertEquals(HttpStatus.CREATED, disburseResp.getStatusCode());
        long loanAccountId = objectMapper.readTree(disburseResp.getBody()).get("data").get("loanAccountId").asLong();

        // 5. Repay disbursed loan
        String repayPayload = "{\"idempotencyKey\":\"HTTP-IT-REPAY-001\",\"amount\":3516.63,\"paymentMethod\":\"SIMULATED_GATEWAY\",\"notes\":\"First EMI\"}";
        ResponseEntity<String> repayResp = restTemplate.withBasicAuth("admin", "password")
                .postForEntity("/api/loans/" + loanAccountId + "/repayments",
                        new HttpEntity<>(repayPayload, jsonHeaders()), String.class);
        assertEquals(HttpStatus.CREATED, repayResp.getStatusCode());
        JsonNode repayNode = objectMapper.readTree(repayResp.getBody()).get("data");
        assertEquals("SUCCESS", repayNode.get("status").asText());
    }
}
