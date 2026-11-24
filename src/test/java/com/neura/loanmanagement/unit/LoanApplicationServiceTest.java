package com.neura.loanmanagement.unit;

import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;
import com.neura.loanmanagement.dto.response.LoanApplicationResponse;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.exception.InvalidStateTransitionException;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.mapper.LoanApplicationMapper;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.LoanEligibilityService;
import com.neura.loanmanagement.service.impl.LoanApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Application Service Unit Tests")
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanEligibilityService loanEligibilityService;

    @Mock
    private AuditLogService auditLogService;

    private LoanApplicationMapper loanApplicationMapper;
    private LoanApplicationServiceImpl applicationService;

    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        loanApplicationMapper = new LoanApplicationMapper();
        applicationService = new LoanApplicationServiceImpl(
                loanApplicationRepository,
                customerRepository,
                loanApplicationMapper,
                loanEligibilityService,
                auditLogService
        );

        sampleCustomer = new Customer("Alice", "Brown", "alice@example.com", "+1-555-1111",
                "NAT-1111", "100 Broadway", LocalDate.of(1992, 4, 10),
                new BigDecimal("70000.00"), EmploymentType.SALARIED, 740, null);
        sampleCustomer.setId(10L);
    }

    @Test
    @DisplayName("Should successfully submit a valid loan application")
    void testSubmitApplicationSuccess() {
        LoanApplicationRequest request = new LoanApplicationRequest(
                10L, LoanType.PERSONAL, new BigDecimal("80000.00"),
                new BigDecimal("10.50"), 24, EmploymentType.SALARIED, new BigDecimal("70000.00"));

        when(customerRepository.findById(10L)).thenReturn(Optional.of(sampleCustomer));
        when(loanEligibilityService.checkEligibility(any())).thenReturn(
                new EligibilityResultResponse(true, new BigDecimal("500000.00"), new BigDecimal("10.00"), new BigDecimal("3500.00"), 740));

        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(invocation -> {
            LoanApplication app = invocation.getArgument(0);
            app.setId(101L);
            return app;
        });

        LoanApplicationResponse response = applicationService.submitApplication(request, "admin", "127.0.0.1");

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(LoanApplicationStatus.SUBMITTED, response.getStatus());
        assertEquals(new BigDecimal("80000.00"), response.getRequestedAmount());
        verify(auditLogService, times(1)).logAction(eq("LoanApplication"), eq("101"), eq("SUBMIT_APPLICATION"), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when customer does not exist")
    void testSubmitApplicationCustomerNotFound() {
        LoanApplicationRequest request = new LoanApplicationRequest(
                999L, LoanType.PERSONAL, new BigDecimal("80000.00"),
                new BigDecimal("10.50"), 24, EmploymentType.SALARIED, new BigDecimal("70000.00"));

        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                applicationService.submitApplication(request, "admin", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should approve a SUBMITTED application and update status and approved terms")
    void testApproveApplicationSuccess() {
        LoanApplication application = new LoanApplication(sampleCustomer, LoanType.HOME,
                new BigDecimal("300000.00"), new BigDecimal("8.00"), 120, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        application.setId(201L);
        application.setStatus(LoanApplicationStatus.SUBMITTED);

        when(loanApplicationRepository.findById(201L)).thenReturn(Optional.of(application));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanApprovalRequest request = new LoanApprovalRequest(
                new BigDecimal("280000.00"), 120, new BigDecimal("8.25"), "Approved with adjusted principal");

        LoanApplicationResponse response = applicationService.approveApplication(201L, request, "officer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(LoanApplicationStatus.APPROVED, response.getStatus());
        assertEquals(new BigDecimal("280000.00"), response.getApprovedAmount());
        assertEquals(new BigDecimal("8.25"), response.getApprovedInterestRate());
        assertEquals("Approved with adjusted principal", response.getReviewNotes());
    }

    @Test
    @DisplayName("Should reject a SUBMITTED application and record rejection reason")
    void testRejectApplicationSuccess() {
        LoanApplication application = new LoanApplication(sampleCustomer, LoanType.PERSONAL,
                new BigDecimal("100000.00"), new BigDecimal("12.00"), 24, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        application.setId(202L);
        application.setStatus(LoanApplicationStatus.SUBMITTED);

        when(loanApplicationRepository.findById(202L)).thenReturn(Optional.of(application));
        when(loanApplicationRepository.save(any(LoanApplication.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanRejectionRequest request = new LoanRejectionRequest("Applicant debt-to-income too high");

        LoanApplicationResponse response = applicationService.rejectApplication(202L, request, "officer", "127.0.0.1");

        assertNotNull(response);
        assertEquals(LoanApplicationStatus.REJECTED, response.getStatus());
        assertEquals("Applicant debt-to-income too high", response.getRejectionReason());
    }

    @Test
    @DisplayName("Should throw InvalidStateTransitionException when approving already REJECTED loan")
    void testInvalidTransitionApproveRejected() {
        LoanApplication application = new LoanApplication(sampleCustomer, LoanType.PERSONAL,
                new BigDecimal("50000.00"), new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        application.setId(203L);
        application.setStatus(LoanApplicationStatus.REJECTED);

        when(loanApplicationRepository.findById(203L)).thenReturn(Optional.of(application));

        assertThrows(InvalidStateTransitionException.class, () ->
                applicationService.approveApplication(203L, new LoanApprovalRequest(), "officer", "127.0.0.1"));
    }

    @Test
    @DisplayName("Should throw InvalidStateTransitionException when rejecting already CLOSED loan")
    void testInvalidTransitionRejectClosed() {
        LoanApplication application = new LoanApplication(sampleCustomer, LoanType.PERSONAL,
                new BigDecimal("50000.00"), new BigDecimal("10.00"), 12, EmploymentType.SALARIED, new BigDecimal("70000.00"));
        application.setId(204L);
        application.setStatus(LoanApplicationStatus.CLOSED);

        when(loanApplicationRepository.findById(204L)).thenReturn(Optional.of(application));

        assertThrows(InvalidStateTransitionException.class, () ->
                applicationService.rejectApplication(204L, new LoanRejectionRequest("test"), "officer", "127.0.0.1"));
    }
}
