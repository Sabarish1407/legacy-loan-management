package com.neura.loanmanagement.unit;

import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.dto.response.LoanRepaymentResponse;
import com.neura.loanmanagement.dto.response.OutstandingBalanceResponse;
import com.neura.loanmanagement.entity.*;
import com.neura.loanmanagement.entity.enums.*;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.exception.DuplicateRepaymentException;
import com.neura.loanmanagement.integration.payment.SimulatedPaymentGateway;
import com.neura.loanmanagement.mapper.RepaymentMapper;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.repository.LoanRepaymentRepository;
import com.neura.loanmanagement.repository.RepaymentScheduleRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.impl.LoanRepaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Repayment Service Unit Tests")
class LoanRepaymentServiceTest {

    @Mock
    private LoanAccountRepository loanAccountRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Mock
    private LoanRepaymentRepository loanRepaymentRepository;

    @Mock
    private AuditLogService auditLogService;

    private SimulatedPaymentGateway paymentGateway;
    private RepaymentMapper repaymentMapper;
    private LoanRepaymentServiceImpl repaymentService;

    private Customer customer;
    private LoanApplication application;
    private LoanAccount account;
    private List<RepaymentSchedule> schedules;

    @BeforeEach
    void setUp() {
        paymentGateway = new SimulatedPaymentGateway("SUCCESS");
        repaymentMapper = new RepaymentMapper();
        repaymentService = new LoanRepaymentServiceImpl(
                loanAccountRepository,
                loanApplicationRepository,
                repaymentScheduleRepository,
                loanRepaymentRepository,
                paymentGateway,
                repaymentMapper,
                auditLogService
        );

        customer = new Customer("Charlie", "Davis", "charlie@example.com", "+1-555-3333",
                "NAT-3333", "300 Elm St", LocalDate.of(1985, 3, 15),
                new BigDecimal("90000.00"), EmploymentType.SALARIED, 750, null);
        customer.setId(30L);

        application = new LoanApplication(customer, LoanType.PERSONAL,
                new BigDecimal("50000.00"), new BigDecimal("10.00"), 3,
                EmploymentType.SALARIED, new BigDecimal("90000.00"));
        application.setId(401L);
        application.setStatus(LoanApplicationStatus.DISBURSED);

        account = new LoanAccount(application, customer, "LN-401-TEST",
                new BigDecimal("50000.00"), new BigDecimal("10.00"), 3,
                new BigDecimal("16945.00"), new BigDecimal("50000.00"), LocalDateTime.now());
        account.setId(501L);

        schedules = new ArrayList<>();
        schedules.add(new RepaymentSchedule(account, 1, LocalDate.now().plusMonths(1),
                new BigDecimal("16528.33"), new BigDecimal("416.67"), new BigDecimal("16945.00"), new BigDecimal("33471.67")));
        schedules.add(new RepaymentSchedule(account, 2, LocalDate.now().plusMonths(2),
                new BigDecimal("16666.07"), new BigDecimal("278.93"), new BigDecimal("16945.00"), new BigDecimal("16805.60")));
        schedules.add(new RepaymentSchedule(account, 3, LocalDate.now().plusMonths(3),
                new BigDecimal("16805.60"), new BigDecimal("140.05"), new BigDecimal("16945.65"), BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should successfully process single full EMI repayment")
    void testProcessFullEmiRepayment() {
        when(loanRepaymentRepository.existsByIdempotencyKey("KEY-001")).thenReturn(false);
        when(loanAccountRepository.findById(501L)).thenReturn(Optional.of(account));
        when(repaymentScheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(501L)).thenReturn(schedules);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenAnswer(inv -> {
            LoanRepayment r = inv.getArgument(0);
            r.setId(601L);
            return r;
        });

        LoanRepaymentRequest request = new LoanRepaymentRequest("KEY-001", new BigDecimal("16945.00"),
                PaymentMethod.SIMULATED_GATEWAY, "First EMI payment");

        LoanRepaymentResponse response = repaymentService.processRepayment(501L, request, "customer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(601L, response.getRepaymentId());
        assertEquals("KEY-001", response.getIdempotencyKey());
        assertEquals(RepaymentStatus.SUCCESS, response.getStatus());
        assertEquals(ScheduleStatus.PAID, schedules.get(0).getStatus());
        assertTrue(account.getOutstandingPrincipal().compareTo(new BigDecimal("50000.00")) < 0);
        assertEquals(LoanAccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    @DisplayName("Should process partial repayment and mark schedule as PARTIALLY_PAID")
    void testProcessPartialRepayment() {
        when(loanRepaymentRepository.existsByIdempotencyKey("KEY-PARTIAL-1")).thenReturn(false);
        when(loanAccountRepository.findById(501L)).thenReturn(Optional.of(account));
        when(repaymentScheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(501L)).thenReturn(schedules);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenAnswer(inv -> {
            LoanRepayment r = inv.getArgument(0);
            r.setId(602L);
            return r;
        });

        LoanRepaymentRequest request = new LoanRepaymentRequest("KEY-PARTIAL-1", new BigDecimal("8000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Half installment payment");

        LoanRepaymentResponse response = repaymentService.processRepayment(501L, request, "customer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(ScheduleStatus.PARTIALLY_PAID, schedules.get(0).getStatus());
        assertEquals(new BigDecimal("8000.00"), schedules.get(0).getPaidAmount());
        assertEquals(LoanAccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    @DisplayName("Should prevent duplicate repayment using same idempotency key")
    void testPreventDuplicateRepaymentByIdempotency() {
        when(loanRepaymentRepository.existsByIdempotencyKey("KEY-DUP")).thenReturn(true);

        LoanRepaymentRequest request = new LoanRepaymentRequest("KEY-DUP", new BigDecimal("1000.00"),
                PaymentMethod.SIMULATED_GATEWAY, null);

        assertThrows(DuplicateRepaymentException.class, () ->
                repaymentService.processRepayment(501L, request, "customer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should close loan account and application when loan is fully paid off")
    void testLoanClosureOnFullPayoff() {
        account.setOutstandingPrincipal(new BigDecimal("10000.00"));
        when(loanRepaymentRepository.existsByIdempotencyKey("KEY-CLOSURE")).thenReturn(false);
        when(loanAccountRepository.findById(501L)).thenReturn(Optional.of(account));
        when(repaymentScheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(501L)).thenReturn(schedules);
        when(loanRepaymentRepository.save(any(LoanRepayment.class))).thenAnswer(inv -> {
            LoanRepayment r = inv.getArgument(0);
            r.setId(603L);
            return r;
        });

        // Pay more than remaining outstanding principal
        LoanRepaymentRequest request = new LoanRepaymentRequest("KEY-CLOSURE", new BigDecimal("60000.00"),
                PaymentMethod.SIMULATED_GATEWAY, "Final payoff");

        LoanRepaymentResponse response = repaymentService.processRepayment(501L, request, "customer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(0, account.getOutstandingPrincipal().compareTo(BigDecimal.ZERO));
        assertEquals(LoanAccountStatus.CLOSED, account.getStatus());
        assertNotNull(account.getClosedAt());
        assertEquals(LoanApplicationStatus.CLOSED, application.getStatus());
    }

    @Test
    @DisplayName("Should reject repayment when account is already CLOSED")
    void testRejectRepaymentOnClosedAccount() {
        account.setStatus(LoanAccountStatus.CLOSED);
        when(loanRepaymentRepository.existsByIdempotencyKey("KEY-CLOSED")).thenReturn(false);
        when(loanAccountRepository.findById(501L)).thenReturn(Optional.of(account));

        LoanRepaymentRequest request = new LoanRepaymentRequest("KEY-CLOSED", new BigDecimal("1000.00"),
                PaymentMethod.SIMULATED_GATEWAY, null);

        assertThrows(BusinessRuleException.class, () ->
                repaymentService.processRepayment(501L, request, "customer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should calculate outstanding balance correctly with next due date and remaining count")
    void testGetOutstandingBalance() {
        when(loanAccountRepository.findById(501L)).thenReturn(Optional.of(account));
        when(repaymentScheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(501L)).thenReturn(schedules);

        OutstandingBalanceResponse balance = repaymentService.getOutstandingBalance(501L);

        assertNotNull(balance);
        assertEquals(501L, balance.getLoanAccountId());
        assertEquals(new BigDecimal("50000.00"), balance.getOutstandingPrincipal());
        assertEquals(3, balance.getRemainingInstallments());
        assertEquals(3, balance.getTotalInstallments());
        assertEquals(schedules.get(0).getDueDate(), balance.getNextDueDate());
        assertEquals(schedules.get(0).getTotalEmi(), balance.getNextDueAmount());
    }
}
