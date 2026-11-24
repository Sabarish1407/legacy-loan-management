package com.neura.loanmanagement.integration;

import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.entity.*;
import com.neura.loanmanagement.entity.enums.*;
import com.neura.loanmanagement.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Loan Entity Relationships and Persistence Integration Tests")
class LoanPersistenceIT {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private DisbursementRepository disbursementRepository;

    @Autowired
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;

    @Test
    @DisplayName("Should successfully persist full loan relationship graph")
    void testFullLoanEntityGraphPersistence() {
        // 1. Customer
        Customer customer = new Customer("Jordan", "Lee", "jordan.lee@example.com", "+1-555-4002",
                "NAT-ID-JL-1001", "200 Market Street", LocalDate.of(1982, 1, 20),
                new BigDecimal("80000.00"), EmploymentType.BUSINESS, 790, KycStatus.VERIFIED);
        customer = customerRepository.save(customer);
        assertNotNull(customer.getId());

        // 2. LoanApplication
        LoanApplication app = new LoanApplication(customer, LoanType.HOME, new BigDecimal("250000.00"),
                new BigDecimal("8.50"), 24, EmploymentType.BUSINESS, new BigDecimal("80000.00"));
        app.setStatus(LoanApplicationStatus.DISBURSED);
        app = loanApplicationRepository.save(app);
        assertNotNull(app.getId());

        // 3. LoanAccount
        LoanAccount account = new LoanAccount(app, customer, "LN-REL-TEST-001",
                new BigDecimal("250000.00"), new BigDecimal("8.50"), 24,
                new BigDecimal("11364.50"), new BigDecimal("250000.00"), LocalDateTime.now());
        account = loanAccountRepository.save(account);
        assertNotNull(account.getId());

        // 4. Disbursement
        Disbursement disbursement = new Disbursement(app, account, new BigDecimal("250000.00"),
                LocalDateTime.now(), "LN-REL-TEST-001", "DSB-REL-001");
        disbursement = disbursementRepository.save(disbursement);
        assertNotNull(disbursement.getId());

        // 5. RepaymentSchedule
        RepaymentSchedule schedule1 = new RepaymentSchedule(account, 1, LocalDate.now().plusMonths(1),
                new BigDecimal("9593.67"), new BigDecimal("1770.83"),
                new BigDecimal("11364.50"), new BigDecimal("240406.33"));
        repaymentScheduleRepository.save(schedule1);

        // 6. LoanRepayment
        LoanRepayment repayment = new LoanRepayment(account, "GW-REL-001", "IDEM-REL-001",
                new BigDecimal("11364.50"), LocalDateTime.now(), PaymentMethod.SIMULATED_GATEWAY,
                new BigDecimal("9593.67"), new BigDecimal("1770.83"), BigDecimal.ZERO,
                RepaymentStatus.SUCCESS, "First installment");
        repayment = loanRepaymentRepository.save(repayment);
        assertNotNull(repayment.getId());

        // Verify relationships via queries
        List<LoanApplication> customerApps = loanApplicationRepository.findByCustomerId(customer.getId());
        assertEquals(1, customerApps.size());
        assertEquals(app.getId(), customerApps.get(0).getId());

        List<LoanAccount> customerAccounts = loanAccountRepository.findByCustomerId(customer.getId());
        assertEquals(1, customerAccounts.size());
        assertEquals(account.getId(), customerAccounts.get(0).getId());

        List<RepaymentSchedule> schedules = repaymentScheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(account.getId());
        assertEquals(1, schedules.size());

        List<LoanRepayment> repayments = loanRepaymentRepository.findByLoanAccountIdOrderByPaymentDateDesc(account.getId());
        assertEquals(1, repayments.size());
        assertEquals("IDEM-REL-001", repayments.get(0).getIdempotencyKey());
    }
}
