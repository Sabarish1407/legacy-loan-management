package com.neura.loanmanagement.e2e;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.entity.enums.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("E2E Scenario 1: Complete Successful Loan Lifecycle")
class Scenario1SuccessfulLoanLifecycleE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete Lifecycle: Create Customer -> Submit App -> Eligibility -> Approve -> Disburse -> Schedule -> Repay -> Close")
    void testCompleteLoanLifecycle() throws Exception {
        // Step 1: Create customer
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                "Taylor", "Morgan", "taylor.m@example.com", "+1-555-8001",
                "NAT-E2E-001", "500 Financial Way", LocalDate.of(1987, 8, 14),
                new BigDecimal("80000.00"), EmploymentType.SALARIED, 760);

        MvcResult custResult = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode custNode = objectMapper.readTree(custResult.getResponse().getContentAsString());
        long customerId = custNode.get("data").get("id").asLong();
        assertTrue(customerId > 0);

        // Step 2: Submit loan application
        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("60000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("80000.00"));

        MvcResult appResult = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andReturn();

        JsonNode appNode = objectMapper.readTree(appResult.getResponse().getContentAsString());
        long applicationId = appNode.get("data").get("id").asLong();

        // Step 3: Eligibility assessment
        String eligibilityPayload = String.format("{\"customerId\":%d,\"loanType\":\"PERSONAL\",\"requestedAmount\":60000.00,\"tenureMonths\":12,\"monthlyIncome\":80000.00,\"employmentType\":\"SALARIED\"}", customerId);
        mockMvc.perform(post("/api/loans/eligibility")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eligibilityPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(true));

        // Step 4: Approve application
        LoanApprovalRequest approvalRequest = new LoanApprovalRequest(
                new BigDecimal("60000.00"), 12, new BigDecimal("10.00"), "Underwriting approval granted");

        mockMvc.perform(patch("/api/loans/applications/" + applicationId + "/approve")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        // Step 5: Disburse the loan
        LoanDisbursementRequest disbRequest = new LoanDisbursementRequest(
                new BigDecimal("60000.00"), null, "LN-E2E-ACCT-1");

        MvcResult disbResult = mockMvc.perform(post("/api/loans/" + applicationId + "/disburse")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disbRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"))
                .andReturn();

        JsonNode disbNode = objectMapper.readTree(disbResult.getResponse().getContentAsString());
        long loanAccountId = disbNode.get("data").get("loanAccountId").asLong();

        // Step 6: Generate and verify EMI schedule
        MvcResult scheduleResult = mockMvc.perform(get("/api/loans/" + applicationId + "/schedule")
                        .with(httpBasic("officer", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.schedules.length()").value(12))
                .andReturn();

        JsonNode schedNode = objectMapper.readTree(scheduleResult.getResponse().getContentAsString());
        double monthlyEmi = schedNode.get("data").get("monthlyEmi").asDouble();
        assertTrue(monthlyEmi > 5000.0 && monthlyEmi < 6000.0);

        // Step 7: Process monthly repayment
        LoanRepaymentRequest repaymentRequest = new LoanRepaymentRequest(
                "IDEM-E2E-PAY-1", new BigDecimal("10000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Month 1 Repayment");

        mockMvc.perform(post("/api/loans/" + applicationId + "/repayments")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUCCESS"));

        // Step 8: Verify outstanding balance reduced
        MvcResult balanceResult = mockMvc.perform(get("/api/loans/" + applicationId + "/balance")
                        .with(httpBasic("officer", "password")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode balNode = objectMapper.readTree(balanceResult.getResponse().getContentAsString());
        double outstanding = balNode.get("data").get("outstandingPrincipal").asDouble();
        assertTrue(outstanding < 60000.00);

        // Step 9: Pay off remainder to close loan
        LoanRepaymentRequest payoffRequest = new LoanRepaymentRequest(
                "IDEM-E2E-PAYOFF", new BigDecimal("55000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Full early payoff");

        mockMvc.perform(post("/api/loans/" + applicationId + "/repayments")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payoffRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.loanAccountStatus").value("CLOSED"))
                .andExpect(jsonPath("$.data.remainingOutstandingPrincipal").value(0.0));

        // Verify loan application status closed
        mockMvc.perform(get("/api/loans/applications/" + applicationId)
                        .with(httpBasic("officer", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }
}
