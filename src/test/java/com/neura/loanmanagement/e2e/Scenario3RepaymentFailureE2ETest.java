package com.neura.loanmanagement.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("E2E Scenario 3: Repayment Failure and Consistency")
class Scenario3RepaymentFailureE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create/Approve/Disburse loan -> Submit failing payment -> Verify 422 error & balance unchanged")
    void testRepaymentFailureWorkflow() throws Exception {
        // 1. Create customer and application
        CreateCustomerRequest custRequest = new CreateCustomerRequest(
                "Dakota", "Hayes", "dakota.h@example.com", "+1-555-8003",
                "NAT-E2E-003", "700 Edge Road", LocalDate.of(1991, 7, 11),
                new BigDecimal("90000.00"), EmploymentType.SALARIED, 770);

        MvcResult custResult = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(custRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long customerId = objectMapper.readTree(custResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("40000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("90000.00"));

        MvcResult appResult = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        long applicationId = objectMapper.readTree(appResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Approve loan
        mockMvc.perform(patch("/api/loans/applications/" + applicationId + "/approve")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanApprovalRequest(new BigDecimal("40000.00"), 12, new BigDecimal("10.00"), "OK"))))
                .andExpect(status().isOk());

        // 2. Disburse loan
        mockMvc.perform(post("/api/loans/" + applicationId + "/disburse")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanDisbursementRequest(new BigDecimal("40000.00"), null, "LN-FAIL-TEST"))))
                .andExpect(status().isCreated());

        // Check initial balance
        MvcResult balBefore = mockMvc.perform(get("/api/loans/" + applicationId + "/balance")
                        .with(httpBasic("officer", "password")))
                .andExpect(status().isOk())
                .andReturn();
        double balanceBefore = objectMapper.readTree(balBefore.getResponse().getContentAsString()).get("data").get("outstandingPrincipal").asDouble();

        // 3. Submit invalid repayment (trigger simulated gateway failure via special instruction)
        LoanRepaymentRequest invalidRepayment = new LoanRepaymentRequest(
                "IDEM-FAIL-001", new BigDecimal("5000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "SIMULATE_GATEWAY_FAILURE");

        // 4. Verify appropriate error response (422 Unprocessable Entity)
        mockMvc.perform(post("/api/loans/" + applicationId + "/repayments")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRepayment)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        // 5. Verify database consistency: outstanding balance remains unchanged
        MvcResult balAfter = mockMvc.perform(get("/api/loans/" + applicationId + "/balance")
                        .with(httpBasic("officer", "password")))
                .andExpect(status().isOk())
                .andReturn();
        double balanceAfter = objectMapper.readTree(balAfter.getResponse().getContentAsString()).get("data").get("outstandingPrincipal").asDouble();

        assertEquals(balanceBefore, balanceAfter, 0.001, "Outstanding balance must not change on failed payment transaction");
    }
}
