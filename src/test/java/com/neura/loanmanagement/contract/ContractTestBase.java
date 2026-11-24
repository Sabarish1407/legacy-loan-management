package com.neura.loanmanagement.contract;

import com.neura.loanmanagement.LoanManagementApplication;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
public abstract class ContractTestBase {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(webApplicationContext)
                        .apply(SecurityMockMvcConfigurers.springSecurity())
                        .build()
        );

        // Ensure customer 1 exists
        Integer customerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customers WHERE id = 1", Integer.class);
        if (customerCount == null || customerCount == 0) {
            jdbcTemplate.update(
                    "INSERT INTO customers (id, first_name, last_name, email, phone, national_id, address, date_of_birth, monthly_income, employment_type, credit_score, kyc_status) " +
                            "VALUES (1, 'John', 'Doe', 'contract.john@example.com', '+1-555-0100', 'NAT-CTR-001', '100 Broadway', '1985-05-15', 75000.00, 'SALARIED', 750, 'VERIFIED')");
        }

        // Clean dependent tables before each test
        jdbcTemplate.update("DELETE FROM loan_repayments");
        jdbcTemplate.update("DELETE FROM repayment_schedules");
        jdbcTemplate.update("DELETE FROM disbursements");
        jdbcTemplate.update("DELETE FROM loan_accounts");
        jdbcTemplate.update("DELETE FROM loan_applications");

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Date nextDue = Date.valueOf(LocalDate.now().plusMonths(1));

        // Insert fresh contract fixtures with explicit IDs
        jdbcTemplate.update(
                "INSERT INTO loan_applications (id, customer_id, loan_type, requested_amount, interest_rate, tenure_months, employment_type, monthly_income, application_date, status) " +
                        "VALUES (1, 1, 'PERSONAL', 50000.00, 10.00, 12, 'SALARIED', 75000.00, ?, 'SUBMITTED')",
                now);

        jdbcTemplate.update(
                "INSERT INTO loan_applications (id, customer_id, loan_type, requested_amount, interest_rate, tenure_months, employment_type, monthly_income, application_date, status) " +
                        "VALUES (2, 1, 'PERSONAL', 30000.00, 10.00, 12, 'SALARIED', 75000.00, ?, 'SUBMITTED')",
                now);

        jdbcTemplate.update(
                "INSERT INTO loan_applications (id, customer_id, loan_type, requested_amount, interest_rate, tenure_months, employment_type, monthly_income, application_date, status, approved_amount, approved_interest_rate, approved_tenure_months) " +
                        "VALUES (3, 1, 'VEHICLE', 40000.00, 9.00, 12, 'SALARIED', 75000.00, ?, 'APPROVED', 40000.00, 9.00, 12)",
                now);

        jdbcTemplate.update(
                "INSERT INTO loan_accounts (id, application_id, customer_id, account_number, principal_amount, annual_interest_rate, tenure_months, monthly_emi, outstanding_principal, disbursement_date, status) " +
                        "VALUES (1, 1, 1, 'LN-CTR-ACC-1', 50000.00, 10.00, 12, 4395.79, 50000.00, ?, 'ACTIVE')",
                now);

        jdbcTemplate.update(
                "INSERT INTO repayment_schedules (id, loan_account_id, installment_number, due_date, principal_amount, interest_amount, total_emi, outstanding_principal, paid_amount, status) " +
                        "VALUES (1, 1, 1, ?, 3979.12, 416.67, 4395.79, 46020.88, 0.00, 'PENDING')",
                nextDue);

        // Advance sequences past the explicit IDs
        try {
            jdbcTemplate.update("ALTER TABLE loan_applications ALTER COLUMN id RESTART WITH 100");
            jdbcTemplate.update("ALTER TABLE loan_accounts ALTER COLUMN id RESTART WITH 100");
            jdbcTemplate.update("ALTER TABLE disbursements ALTER COLUMN id RESTART WITH 100");
            jdbcTemplate.update("ALTER TABLE repayment_schedules ALTER COLUMN id RESTART WITH 100");
            jdbcTemplate.update("ALTER TABLE loan_repayments ALTER COLUMN id RESTART WITH 100");
        } catch (Exception ignored) {
            // Safe fallback if DB dialect differs
        }
    }
}
