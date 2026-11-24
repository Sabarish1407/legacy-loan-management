package com.neura.loanmanagement.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("E2E Scenario 4: Customer Data Isolation")
class Scenario4CustomerIsolationE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Create 2 customers with loans -> Customer 1 attempts to access Customer 2 loan -> 403 Forbidden")
    void testCustomerDataIsolation() throws Exception {
        // 1. Create Customer 1
        CreateCustomerRequest cust1Req = new CreateCustomerRequest(
                "CustOne", "Alpha", "cust1.alpha@example.com", "+1-555-9001",
                "NAT-ISO-001", "101 Alpha St", LocalDate.of(1990, 1, 1),
                new BigDecimal("70000.00"), EmploymentType.SALARIED, 750);

        MvcResult c1Res = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cust1Req)))
                .andExpect(status().isCreated())
                .andReturn();
        long cust1Id = objectMapper.readTree(c1Res.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 1. Create Customer 2
        CreateCustomerRequest cust2Req = new CreateCustomerRequest(
                "CustTwo", "Beta", "cust2.beta@example.com", "+1-555-9002",
                "NAT-ISO-002", "202 Beta St", LocalDate.of(1992, 2, 2),
                new BigDecimal("85000.00"), EmploymentType.SALARIED, 780);

        MvcResult c2Res = mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cust2Req)))
                .andExpect(status().isCreated())
                .andReturn();
        long cust2Id = objectMapper.readTree(c2Res.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Create Loan for Customer 1
        LoanApplicationRequest app1Req = new LoanApplicationRequest(
                cust1Id, LoanType.PERSONAL, new BigDecimal("30000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        MvcResult app1Res = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(app1Req)))
                .andExpect(status().isCreated())
                .andReturn();
        long app1Id = objectMapper.readTree(app1Res.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Create Loan for Customer 2
        LoanApplicationRequest app2Req = new LoanApplicationRequest(
                cust2Id, LoanType.PERSONAL, new BigDecimal("50000.00"),
                new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("85000.00"));
        MvcResult app2Res = mockMvc.perform(post("/api/loans/applications")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(app2Req)))
                .andExpect(status().isCreated())
                .andReturn();
        long app2Id = objectMapper.readTree(app2Res.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 3. Authenticate as Customer 1 ('cust1.alpha@example.com' password 'password')
        // Can access own customer profile
        mockMvc.perform(get("/api/customers/" + cust1Id)
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isOk());

        // Can access own loan application
        mockMvc.perform(get("/api/loans/applications/" + app1Id)
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isOk());

        // 4 & 5. Attempt to access Customer 2's profile -> 403 Forbidden
        mockMvc.perform(get("/api/customers/" + cust2Id)
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isForbidden());

        // Attempt to access Customer 2's loans -> 403 Forbidden
        mockMvc.perform(get("/api/customers/" + cust2Id + "/loans")
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isForbidden());

        // Attempt to access Customer 2's loan application -> 403 Forbidden
        mockMvc.perform(get("/api/loans/applications/" + app2Id)
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isForbidden());

        // Attempt to approve loan (administrative endpoint) -> 403 Forbidden
        mockMvc.perform(patch("/api/loans/applications/" + app1Id + "/approve")
                        .with(httpBasic("cust1.alpha@example.com", "password")))
                .andExpect(status().isForbidden());
    }
}
