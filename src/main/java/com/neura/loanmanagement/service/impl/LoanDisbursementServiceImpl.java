package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.response.LoanDisbursementResponse;
import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;
import com.neura.loanmanagement.entity.*;
import com.neura.loanmanagement.entity.enums.DisbursementStatus;
import com.neura.loanmanagement.entity.enums.LoanAccountStatus;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
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
import com.neura.loanmanagement.service.LoanDisbursementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoanDisbursementServiceImpl implements LoanDisbursementService {

    private static final Logger log = LoggerFactory.getLogger(LoanDisbursementServiceImpl.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final DisbursementRepository disbursementRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final EmiCalculatorService emiCalculatorService;
    private final DisbursementMapper disbursementMapper;
    private final AuditLogService auditLogService;

    public LoanDisbursementServiceImpl(LoanApplicationRepository loanApplicationRepository,
                                       LoanAccountRepository loanAccountRepository,
                                       DisbursementRepository disbursementRepository,
                                       RepaymentScheduleRepository repaymentScheduleRepository,
                                       EmiCalculatorService emiCalculatorService,
                                       DisbursementMapper disbursementMapper,
                                       AuditLogService auditLogService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.disbursementRepository = disbursementRepository;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.emiCalculatorService = emiCalculatorService;
        this.disbursementMapper = disbursementMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public LoanDisbursementResponse disburseLoan(Long loanApplicationId, LoanDisbursementRequest request,
                                                 String performedBy, String clientIp) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", loanApplicationId));

        // Check if already disbursed
        if (application.getStatus() == LoanApplicationStatus.DISBURSED ||
                disbursementRepository.existsByApplicationId(loanApplicationId)) {
            throw new DuplicateDisbursementException(loanApplicationId);
        }

        // Validate state transition
        if (!application.getStatus().canTransitionTo(LoanApplicationStatus.DISBURSED)) {
            throw new InvalidStateTransitionException(application.getStatus(), LoanApplicationStatus.DISBURSED);
        }

        BigDecimal principal = application.getApprovedAmount() != null
                ? application.getApprovedAmount() : application.getRequestedAmount();
        if (request != null && request.getAmount() != null) {
            principal = request.getAmount();
        }

        BigDecimal interestRate = application.getApprovedInterestRate() != null
                ? application.getApprovedInterestRate() : application.getInterestRate();
        Integer tenureMonths = application.getApprovedTenureMonths() != null
                ? application.getApprovedTenureMonths() : application.getTenureMonths();

        LocalDateTime disbDate = (request != null && request.getDisbursementDate() != null)
                ? request.getDisbursementDate() : LocalDateTime.now();

        String accNumber = (request != null && request.getAccountNumber() != null && !request.getAccountNumber().isBlank())
                ? request.getAccountNumber()
                : "LN-" + application.getId() + "-" + System.currentTimeMillis();

        BigDecimal monthlyEmi = emiCalculatorService.calculateMonthlyEmi(principal, interestRate, tenureMonths);

        // Create Loan Account
        LoanAccount account = new LoanAccount(
                application,
                application.getCustomer(),
                accNumber,
                principal,
                interestRate,
                tenureMonths,
                monthlyEmi,
                principal,
                disbDate
        );
        LoanAccount savedAccount = loanAccountRepository.save(account);

        // Generate and save repayment schedule
        List<LoanScheduleItemResponse> scheduleItems = emiCalculatorService.generateRepaymentSchedule(
                principal, interestRate, tenureMonths, disbDate.toLocalDate());

        for (LoanScheduleItemResponse item : scheduleItems) {
            RepaymentSchedule schedule = new RepaymentSchedule(
                    savedAccount,
                    item.getInstallmentNumber(),
                    item.getDueDate(),
                    item.getPrincipalAmount(),
                    item.getInterestAmount(),
                    item.getTotalEmi(),
                    item.getOutstandingPrincipal()
            );
            repaymentScheduleRepository.save(schedule);
        }

        // Create Disbursement record
        String refNumber = "DSB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Disbursement disbursement = new Disbursement(
                application,
                savedAccount,
                principal,
                disbDate,
                accNumber,
                refNumber
        );
        Disbursement savedDisbursement = disbursementRepository.save(disbursement);

        // Update application state
        application.setStatus(LoanApplicationStatus.DISBURSED);
        loanApplicationRepository.save(application);

        auditLogService.logAction("Disbursement", savedDisbursement.getId().toString(), "DISBURSE_LOAN",
                performedBy, String.format("Disbursed amount %s to account %s for application %d",
                        principal, accNumber, loanApplicationId), clientIp);

        return disbursementMapper.toResponse(savedDisbursement);
    }
}
