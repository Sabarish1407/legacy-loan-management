package com.neura.loanmanagement.e2e;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("E2E Scenario 2: Rejected Loan Workflow")
class Scenario2RejectedLoanE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create customer -> Submit -> Eligibility (fails) -> Reject -> Disbursement prevented")
    void testRejectedLoanLifecycle() throws Exception {
        // 1. Create customer
        CreateCustomerRequest customerRequest = new CreateCustomerRequest(
                "Riley", "Green", "riley.green@example.com", "+1-555-8002",
                "NAT-E2E-002", "600 Reject Blvd", LocalDate.of(1995, 2, 10),
                new BigDecimal("20000.00"), EmploymentType.UNEMPLOYED, 520); // Low credit score & unemployed

        MvcResult custResult = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode custNode = objectMapper.readTree(custResult.getResponse().getContentAsString());
        long customerId = custNode.get("data").get("id").asLong();

        // 2. Submit application
        LoanApplicationRequest appRequest = new LoanApplicationRequest(
                customerId, LoanType.PERSONAL, new BigDecimal("100000.00"),
                new BigDecimal("12.00"), 12, EmploymentType.UNEMPLOYED, new BigDecimal("20000.00"));

        MvcResult appResult = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andReturn();

        JsonNode appNode = objectMapper.readTree(appResult.getResponse().getContentAsString());
        long applicationId = appNode.get("data").get("id").asLong();

        // 3. Eligibility check confirms ineligibility
        String eligibilityPayload = String.format("{\"customerId\":%d,\"loanType\":\"PERSONAL\",\"requestedAmount\":100000.00,\"tenureMonths\":12,\"monthlyIncome\":20000.00,\"employmentType\":\"UNEMPLOYED\",\"creditScore\":520}", customerId);
        mockMvc.perform(post("/api/loans/eligibility")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eligibilityPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eligible").value(false));

        // 4. Reject application
        LoanRejectionRequest rejectionRequest = new LoanRejectionRequest("Applicant does not meet underwriting criteria");
        mockMvc.perform(patch("/api/loans/applications/" + applicationId + "/reject")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.rejectionReason").value("Applicant does not meet underwriting criteria"));

        // 5. Verify that disbursement is prevented on REJECTED loan (409 Conflict)
        LoanDisbursementRequest disbRequest = new LoanDisbursementRequest(
                new BigDecimal("100000.00"), null, "LN-REJECTED-ACCT");

        mockMvc.perform(post("/api/loans/" + applicationId + "/disburse")
                        .with(httpBasic("officer", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disbRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
