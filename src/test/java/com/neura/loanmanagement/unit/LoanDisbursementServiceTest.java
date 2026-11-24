package com.neura.loanmanagement.unit;

import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.response.LoanDisbursementResponse;
import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;
import com.neura.loanmanagement.entity.*;
import com.neura.loanmanagement.entity.enums.*;
import com.neura.loanmanagement.exception.DuplicateDisbursementException;
import com.neura.loanmanagement.exception.InvalidStateTransitionException;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.mapper.DisbursementMapper;
import com.neura.loanmanagement.repository.DisbursementRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.repository.RepaymentScheduleRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.EmiCalculatorService;
import com.neura.loanmanagement.service.impl.LoanDisbursementServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Disbursement Service Unit Tests")
class LoanDisbursementServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private LoanAccountRepository loanAccountRepository;

    @Mock
    private DisbursementRepository disbursementRepository;

    @Mock
    private RepaymentScheduleRepository repaymentScheduleRepository;

    @Mock
    private EmiCalculatorService emiCalculatorService;

    @Mock
    private AuditLogService auditLogService;

    private DisbursementMapper disbursementMapper;
    private LoanDisbursementServiceImpl disbursementService;

    private Customer customer;
    private LoanApplication approvedApp;

    @BeforeEach
    void setUp() {
        disbursementMapper = new DisbursementMapper();
        disbursementService = new LoanDisbursementServiceImpl(
                loanApplicationRepository,
                loanAccountRepository,
                disbursementRepository,
                repaymentScheduleRepository,
                emiCalculatorService,
                disbursementMapper,
                auditLogService
        );

        customer = new Customer("Bob", "Miller", "bob@example.com", "+1-555-2222",
                "NAT-2222", "200 Fifth Ave", LocalDate.of(1988, 6, 20),
                new BigDecimal("85000.00"), EmploymentType.SALARIED, 760, null);
        customer.setId(20L);

        approvedApp = new LoanApplication(customer, LoanType.PERSONAL,
                new BigDecimal("120000.00"), new BigDecimal("9.50"), 12,
                EmploymentType.SALARIED, new BigDecimal("85000.00"));
        approvedApp.setId(301L);
        approvedApp.setStatus(LoanApplicationStatus.APPROVED);
        approvedApp.setApprovedAmount(new BigDecimal("120000.00"));
        approvedApp.setApprovedInterestRate(new BigDecimal("9.50"));
        approvedApp.setApprovedTenureMonths(12);
    }

    @Test
    @DisplayName("Should successfully disburse an approved loan and create account and schedules")
    void testDisbursementSuccess() {
        when(loanApplicationRepository.findById(301L)).thenReturn(Optional.of(approvedApp));
        when(disbursementRepository.existsByApplicationId(301L)).thenReturn(false);
        when(emiCalculatorService.calculateMonthlyEmi(any(), any(), anyInt())).thenReturn(new BigDecimal("10523.50"));

        List<LoanScheduleItemResponse> dummySchedules = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            dummySchedules.add(new LoanScheduleItemResponse(i, LocalDate.now().plusMonths(i),
                    new BigDecimal("9500.00"), new BigDecimal("1023.50"),
                    new BigDecimal("10523.50"), new BigDecimal("100000.00"),
                    BigDecimal.ZERO, ScheduleStatus.PENDING, null));
        }
        when(emiCalculatorService.generateRepaymentSchedule(any(), any(), anyInt(), any())).thenReturn(dummySchedules);

        when(loanAccountRepository.save(any(LoanAccount.class))).thenAnswer(inv -> {
            LoanAccount acc = inv.getArgument(0);
            acc.setId(401L);
            return acc;
        });

        when(disbursementRepository.save(any(Disbursement.class))).thenAnswer(inv -> {
            Disbursement d = inv.getArgument(0);
            d.setId(501L);
            return d;
        });

        LoanDisbursementRequest request = new LoanDisbursementRequest(
                new BigDecimal("120000.00"), LocalDateTime.now(), "LN-TEST-401");

        LoanDisbursementResponse response = disbursementService.disburseLoan(301L, request, "officer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(501L, response.getDisbursementId());
        assertEquals(401L, response.getLoanAccountId());
        assertEquals("LN-TEST-401", response.getAccountNumber());
        assertEquals(LoanApplicationStatus.DISBURSED, approvedApp.getStatus());
        verify(repaymentScheduleRepository, times(12)).save(any(RepaymentSchedule.class));
        verify(auditLogService, times(1)).logAction(eq("Disbursement"), eq("501"), eq("DISBURSE_LOAN"), any(), any(), any());
    }

    @Test
    @DisplayName("Should prevent duplicate disbursement when loan is already disbursed")
    void testPreventDuplicateDisbursement() {
        approvedApp.setStatus(LoanApplicationStatus.DISBURSED);
        when(loanApplicationRepository.findById(301L)).thenReturn(Optional.of(approvedApp));

        assertThrows(DuplicateDisbursementException.class, () ->
                disbursementService.disburseLoan(301L, new LoanDisbursementRequest(), "officer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should prevent duplicate disbursement when disbursement record exists")
    void testPreventDuplicateWhenRecordExists() {
        when(loanApplicationRepository.findById(301L)).thenReturn(Optional.of(approvedApp));
        when(disbursementRepository.existsByApplicationId(301L)).thenReturn(true);

        assertThrows(DuplicateDisbursementException.class, () ->
                disbursementService.disburseLoan(301L, new LoanDisbursementRequest(), "officer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should throw InvalidStateTransitionException when application is not APPROVED")
    void testPreventDisbursementUnapproved() {
        approvedApp.setStatus(LoanApplicationStatus.SUBMITTED);
        when(loanApplicationRepository.findById(301L)).thenReturn(Optional.of(approvedApp));
        when(disbursementRepository.existsByApplicationId(301L)).thenReturn(false);

        assertThrows(InvalidStateTransitionException.class, () ->
                disbursementService.disburseLoan(301L, new LoanDisbursementRequest(), "officer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown application ID")
    void testDisbursementApplicationNotFound() {
        when(loanApplicationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                disbursementService.disburseLoan(999L, new LoanDisbursementRequest(), "officer", "127.0.0.1"));
    }
}
