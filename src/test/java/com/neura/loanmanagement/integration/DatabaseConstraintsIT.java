package com.neura.loanmanagement.integration;

import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.LoanAccount;
import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.entity.LoanRepayment;
import com.neura.loanmanagement.entity.enums.*;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.repository.LoanRepaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Database Constraints and Integrity Integration Tests")
class DatabaseConstraintsIT {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;

    @Test
    @DisplayName("Duplicate customer email should violate unique database constraint")
    void testDuplicateCustomerEmailConstraint() {
        Customer c1 = new Customer("Casey", "Stone", "casey.stone@example.com", "+1-555-4004",
                "NAT-CNST-01", "400 Oak Street", LocalDate.of(1980, 3, 25),
                new BigDecimal("50000.00"), EmploymentType.SALARIED, 750, KycStatus.VERIFIED);
        customerRepository.saveAndFlush(c1);

        Customer c2 = new Customer("Casey", "Smith", "casey.stone@example.com", "+1-555-4005",
                "NAT-CNST-02", "400 Oak Street", LocalDate.of(1980, 3, 25),
                new BigDecimal("50000.00"), EmploymentType.SALARIED, 750, KycStatus.VERIFIED);

        assertThrows(DataIntegrityViolationException.class, () -> customerRepository.saveAndFlush(c2));
    }

    @Test
    @DisplayName("Duplicate customer national ID should violate unique database constraint")
    void testDuplicateNationalIdConstraint() {
        Customer c1 = new Customer("Avery", "Parker", "avery.p@example.com", "+1-555-4006",
                "NAT-CNST-AVERY", "500 Cedar Ave", LocalDate.of(1983, 5, 1),
                new BigDecimal("55000.00"), EmploymentType.SALARIED, 680, KycStatus.VERIFIED);
        customerRepository.saveAndFlush(c1);

        Customer c2 = new Customer("Avery", "Paul", "avery.paul@example.com", "+1-555-4007",
                "NAT-CNST-AVERY", "600 Birch Road", LocalDate.of(1983, 5, 1),
                new BigDecimal("55000.00"), EmploymentType.SALARIED, 680, KycStatus.VERIFIED);

        assertThrows(DataIntegrityViolationException.class, () -> customerRepository.saveAndFlush(c2));
    }

    @Test
    @DisplayName("Duplicate repayment idempotency key should violate unique database constraint")
    void testDuplicateRepaymentIdempotencyKeyConstraint() {
        Customer customer = new Customer("Sam", "Jenkins", "sam.jenkins@example.com", "+1-555-4008",
                "NAT-CNST-SAM", "700 Walnut Blvd", LocalDate.of(1975, 2, 19),
                new BigDecimal("65000.00"), EmploymentType.SALARIED, 720, KycStatus.VERIFIED);
        customer = customerRepository.saveAndFlush(customer);

        LoanApplication app = new LoanApplication(customer, LoanType.PERSONAL,
                new BigDecimal("30000.00"), new BigDecimal("9.00"), 12,
                EmploymentType.SALARIED, new BigDecimal("65000.00"));
        app = loanApplicationRepository.saveAndFlush(app);

        LoanAccount account = new LoanAccount(app, customer, "LN-CNST-001",
                new BigDecimal("30000.00"), new BigDecimal("9.00"), 12,
                new BigDecimal("2624.00"), new BigDecimal("30000.00"), LocalDateTime.now());
        account = loanAccountRepository.saveAndFlush(account);

        LoanRepayment r1 = new LoanRepayment(account, "REF-CNST-1", "IDEM-DUPLICATE-KEY",
                new BigDecimal("2624.00"), LocalDateTime.now(), PaymentMethod.SIMULATED_GATEWAY,
                new BigDecimal("2400.00"), new BigDecimal("224.00"), BigDecimal.ZERO, RepaymentStatus.SUCCESS, "R1");
        loanRepaymentRepository.saveAndFlush(r1);

        LoanRepayment r2 = new LoanRepayment(account, "REF-CNST-2", "IDEM-DUPLICATE-KEY",
                new BigDecimal("2624.00"), LocalDateTime.now(), PaymentMethod.SIMULATED_GATEWAY,
                new BigDecimal("2400.00"), new BigDecimal("224.00"), BigDecimal.ZERO, RepaymentStatus.SUCCESS, "R2");

        assertThrows(DataIntegrityViolationException.class, () -> loanRepaymentRepository.saveAndFlush(r2));
    }
}
