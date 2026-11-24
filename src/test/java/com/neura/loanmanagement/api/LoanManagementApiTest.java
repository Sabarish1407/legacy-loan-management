package com.neura.loanmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.entity.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("REST API Controller Tests")
class LoanManagementApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/customers - Success with ADMIN role")
    void testCreateCustomerSuccess() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jordan", "Smith", "jordan.api@example.com", "+1-555-7001",
                "NAT-API-001", "123 Banking Blvd", LocalDate.of(1990, 5, 20),
                new BigDecimal("75000.00"), EmploymentType.SALARIED, 740);

        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("jordan.api@example.com"));
    }

    @Test
    @DisplayName("POST /api/customers - 400 Bad Request on Missing Fields")
    void testCreateCustomerMissingFields() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(); // All fields null

        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("GET /api/customers/999999 - 404 Not Found")
    void testGetCustomerNotFound() throws Exception {
        mockMvc.perform(get("/api/customers/999999")
                        .with(httpBasic("admin", "password")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/customers - 401 Unauthorized when credentials missing")
    void testUnauthenticatedAccess() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Anon", "User", "anon@example.com", "+1-555-7002",
                "NAT-API-002", "123 Secret St", LocalDate.of(1992, 1, 1),
                new BigDecimal("50000.00"), EmploymentType.SALARIED, 700);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/customers - 403 Forbidden when invoked by CUSTOMER role")
    void testCustomerCreationForbiddenForCustomerRole() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Cust", "User", "cust@example.com", "+1-555-7003",
                "NAT-API-003", "123 Customer St", LocalDate.of(1992, 1, 1),
                new BigDecimal("50000.00"), EmploymentType.SALARIED, 700);

        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("john_doe", "password")) // john_doe has CUSTOMER role
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/loans/applications - Validation Error on Invalid Tenure")
    void testLoanApplicationInvalidTenure() throws Exception {
        LoanApplicationRequest request = new LoanApplicationRequest(
                1L, LoanType.PERSONAL, new BigDecimal("50000.00"),
                new BigDecimal("10.00"), 2, // invalid tenure (< 6 months)
                EmploymentType.SALARIED, new BigDecimal("60000.00"));

        mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private Long createAndDisburseLoan() throws Exception {
        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                1L, LoanType.PERSONAL, new BigDecimal("30000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("75000.00"));

        org.springframework.test.web.servlet.MvcResult appRes = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        long appId = objectMapper.readTree(appRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        mockMvc.perform(patch("/api/loans/applications/" + appId + "/approve")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"remarks\":\"Approved for test\"}"))
                .andExpect(status().isOk());

        LoanDisbursementRequest disbRequest = new LoanDisbursementRequest(
                new BigDecimal("30000.00"), null, "ACT-DISB-API-001");

        org.springframework.test.web.servlet.MvcResult disbRes = mockMvc.perform(post("/api/loans/" + appId + "/disburse")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disbRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(disbRes.getResponse().getContentAsString()).get("data").get("loanAccountId").asLong();
    }

    @Test
    @DisplayName("POST /api/loans/1/repayments - 409 Conflict on Duplicate Idempotency Key")
    void testDuplicateRepaymentIdempotencyKey() throws Exception {
        Long loanAccountId = createAndDisburseLoan();

        LoanRepaymentRequest request = new LoanRepaymentRequest(
                "IDEM-API-DUP-KEY", new BigDecimal("1000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "First payment");

        // First attempt succeeds
        mockMvc.perform(post("/api/loans/" + loanAccountId + "/repayments")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second attempt with identical idempotency key returns 409 Conflict
        mockMvc.perform(post("/api/loans/" + loanAccountId + "/repayments")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/loans/eligibility - 200 OK with detailed assessment")
    void testEligibilityAssessmentEndpoint() throws Exception {
        String payload = "{" +
                "\"loanType\":\"PERSONAL\"," +
                "\"requestedAmount\":50000.00," +
                "\"tenureMonths\":12," +
                "\"monthlyIncome\":70000.00," +
                "\"employmentType\":\"SALARIED\"," +
                "\"creditScore\":750" +
                "}";

        mockMvc.perform(post("/api/loans/eligibility")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true))
                .andExpect(jsonPath("$.data.recommendedInterestRate").value(8.50));
    }
}
