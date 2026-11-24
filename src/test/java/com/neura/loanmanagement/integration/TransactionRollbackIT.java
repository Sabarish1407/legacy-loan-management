package com.neura.loanmanagement.integration;

import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.exception.DuplicateDisbursementException;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.DisbursementRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.service.LoanDisbursementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
@DisplayName("Transaction Rollback Integration Tests")
class TransactionRollbackIT {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Autowired
    private DisbursementRepository disbursementRepository;

    @Autowired
    private LoanDisbursementService loanDisbursementService;

    @Test
    @DisplayName("Transaction rollback should occur on failed disbursement without partial data persistence")
    void testDisbursementTransactionRollback() {
        Customer customer = new Customer("Morgan", "Reed", "morgan.reed@example.com", "+1-555-4003",
                "NAT-TX-001", "350 Pine Street", LocalDate.of(1989, 10, 1),
                new BigDecimal("60000.00"), EmploymentType.SALARIED, 730, null);
        customer = customerRepository.save(customer);

        LoanApplication app = new LoanApplication(customer, LoanType.PERSONAL,
                new BigDecimal("50000.00"), new BigDecimal("10.00"), 12,
                EmploymentType.SALARIED, new BigDecimal("60000.00"));
        app.setStatus(LoanApplicationStatus.APPROVED);
        LoanApplication savedApp = loanApplicationRepository.save(app);
        final Long appId = savedApp.getId();

        long initialAccounts = loanAccountRepository.count();
        long initialDisbursements = disbursementRepository.count();

        // 1st disbursement succeeds
        loanDisbursementService.disburseLoan(appId, new LoanDisbursementRequest(), "admin", "127.0.0.1");

        assertEquals(initialAccounts + 1, loanAccountRepository.count());
        assertEquals(initialDisbursements + 1, disbursementRepository.count());

        // 2nd disbursement on same application triggers DuplicateDisbursementException
        assertThrows(DuplicateDisbursementException.class, () ->
                loanDisbursementService.disburseLoan(appId, new LoanDisbursementRequest(), "admin", "127.0.0.1"));

        // Verify counts remain unchanged (no rogue account created)
        assertEquals(initialAccounts + 1, loanAccountRepository.count());
        assertEquals(initialDisbursements + 1, disbursementRepository.count());
    }
}
