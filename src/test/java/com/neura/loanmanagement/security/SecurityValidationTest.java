package com.neura.loanmanagement.security;

import tools.jackson.databind.ObjectMapper;
import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Security & Authorization Compliance Tests")
class SecurityValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Authentication: Invalid credentials return 401 Unauthorized")
    void testInvalidCredentialsRejected() throws Exception {
        mockMvc.perform(get("/api/customers/1")
                        .with(httpBasic("admin", "wrong_password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Authorization: Non-existent user returns 401 Unauthorized")
    void testNonExistentUserRejected() throws Exception {
        mockMvc.perform(get("/api/customers/1")
                        .with(httpBasic("unknown_user", "password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Role Authorization: CUSTOMER cannot access GET /api/customers (list all customers)")
    void testCustomerCannotListAllCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .with(httpBasic("john_doe", "password")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role Authorization: ADMIN has full access to GET /api/customers")
    void testAdminCanListAllCustomers() throws Exception {
        mockMvc.perform(get("/api/customers")
                        .with(httpBasic("admin", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Unauthorized Loan Approval: CUSTOMER role cannot approve loan (403 Forbidden)")
    void testCustomerCannotApproveLoan() throws Exception {
        mockMvc.perform(patch("/api/loans/applications/1/approve")
                        .with(httpBasic("john_doe", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanApprovalRequest(new BigDecimal("10000.00"), 12, new BigDecimal("10.00"), "Notes"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unauthorized Disbursement: CUSTOMER role cannot disburse loan (403 Forbidden)")
    void testCustomerCannotDisburseLoan() throws Exception {
        mockMvc.perform(post("/api/loans/1/disburse")
                        .with(httpBasic("john_doe", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoanDisbursementRequest(new BigDecimal("10000.00"), null, "LN-UNAUTH"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Sensitive Data Protection: Error responses must not expose raw internal passwords or stack traces")
    void testSensitiveDataNotExposedInErrors() throws Exception {
        mockMvc.perform(get("/api/customers/999999")
                        .with(httpBasic("admin", "password")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("org.springframework"))));
    }

    @Test
    @DisplayName("Input Validation: Injection and malicious input rejected by bean validation")
    void testInputValidationRejectsInvalidData() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "<script>alert(1)</script>", "ValidLast", "not-an-email", "+1-555",
                "", "Address", LocalDate.now().plusDays(1), // Future DOB
                new BigDecimal("-500.00"), EmploymentType.SALARIED, 100); // Invalid credit score

        mockMvc.perform(post("/api/customers")
                        .with(httpBasic("admin", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }
}
