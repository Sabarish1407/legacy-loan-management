package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.dto.response.LoanRepaymentResponse;
import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;
import com.neura.loanmanagement.dto.response.OutstandingBalanceResponse;
import com.neura.loanmanagement.dto.response.RepaymentScheduleResponse;
import com.neura.loanmanagement.entity.*;
import com.neura.loanmanagement.entity.enums.LoanAccountStatus;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import com.neura.loanmanagement.entity.enums.RepaymentStatus;
import com.neura.loanmanagement.entity.enums.ScheduleStatus;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.exception.DuplicateRepaymentException;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.integration.payment.SimulatedPaymentGateway;
import com.neura.loanmanagement.mapper.RepaymentMapper;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.repository.LoanRepaymentRepository;
import com.neura.loanmanagement.repository.RepaymentScheduleRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.LoanRepaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LoanRepaymentServiceImpl implements LoanRepaymentService {

    private static final Logger log = LoggerFactory.getLogger(LoanRepaymentServiceImpl.class);

    private final LoanAccountRepository loanAccountRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final RepaymentScheduleRepository repaymentScheduleRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final SimulatedPaymentGateway paymentGateway;
    private final RepaymentMapper repaymentMapper;
    private final AuditLogService auditLogService;

    public LoanRepaymentServiceImpl(LoanAccountRepository loanAccountRepository,
                                   LoanApplicationRepository loanApplicationRepository,
                                   RepaymentScheduleRepository repaymentScheduleRepository,
                                   LoanRepaymentRepository loanRepaymentRepository,
                                   SimulatedPaymentGateway paymentGateway,
                                   RepaymentMapper repaymentMapper,
                                   AuditLogService auditLogService) {
        this.loanAccountRepository = loanAccountRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.repaymentScheduleRepository = repaymentScheduleRepository;
        this.loanRepaymentRepository = loanRepaymentRepository;
        this.paymentGateway = paymentGateway;
        this.repaymentMapper = repaymentMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public LoanRepaymentResponse processRepayment(Long loanId, LoanRepaymentRequest request,
                                                  String performedBy, String clientIp) {
        // Prevent duplicate processing via Idempotency Key
        if (loanRepaymentRepository.existsByIdempotencyKey(request.getIdempotencyKey())) {
            throw new DuplicateRepaymentException(request.getIdempotencyKey());
        }

        LoanAccount account = findAccountByLoanId(loanId);

        if (account.getStatus() == LoanAccountStatus.CLOSED) {
            throw new BusinessRuleException("Loan account is already closed. No further repayments accepted.");
        }

        // Process payment through simulated gateway
        SimulatedPaymentGateway.PaymentResult paymentResult = paymentGateway.processPayment(
                request.getIdempotencyKey(),
                request.getAmount(),
                request.getPaymentMethod(),
                request.getNotes()
        );

        if (!paymentResult.isSuccess()) {
            // Save failed repayment attempt for audit and consistency
            LoanRepayment failedRepayment = new LoanRepayment(
                    account,
                    "FAIL-" + UUID.randomUUID().toString().substring(0, 8),
                    request.getIdempotencyKey(),
                    request.getAmount(),
                    LocalDateTime.now(),
                    request.getPaymentMethod(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    RepaymentStatus.FAILED,
                    paymentResult.getMessage()
            );
            loanRepaymentRepository.save(failedRepayment);
            throw new BusinessRuleException("Payment processing failed: " + paymentResult.getMessage());
        }

        // Allocate payment across schedules (oldest pending installment first)
        List<RepaymentSchedule> schedules = repaymentScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(account.getId());

        BigDecimal remainingPayment = request.getAmount();
        BigDecimal totalPrincipalAllocated = BigDecimal.ZERO;
        BigDecimal totalInterestAllocated = BigDecimal.ZERO;

        for (RepaymentSchedule schedule : schedules) {
            if (remainingPayment.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            if (schedule.getStatus() == ScheduleStatus.PAID) {
                continue;
            }

            BigDecimal dueOnInstallment = schedule.getTotalEmi().subtract(schedule.getPaidAmount());
            BigDecimal paymentToSchedule = remainingPayment.min(dueOnInstallment);

            BigDecimal totalEmi = schedule.getTotalEmi();
            BigDecimal interestRatio = totalEmi.compareTo(BigDecimal.ZERO) > 0
                    ? schedule.getInterestAmount().divide(totalEmi, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal interestPart = paymentToSchedule.multiply(interestRatio).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPart = paymentToSchedule.subtract(interestPart).setScale(2, RoundingMode.HALF_UP);

            totalInterestAllocated = totalInterestAllocated.add(interestPart);
            totalPrincipalAllocated = totalPrincipalAllocated.add(principalPart);

            schedule.setPaidAmount(schedule.getPaidAmount().add(paymentToSchedule));
            if (schedule.getPaidAmount().compareTo(schedule.getTotalEmi()) >= 0) {
                schedule.setStatus(ScheduleStatus.PAID);
                schedule.setPaidDate(LocalDate.now());
            } else {
                schedule.setStatus(ScheduleStatus.PARTIALLY_PAID);
            }

            repaymentScheduleRepository.save(schedule);
            remainingPayment = remainingPayment.subtract(paymentToSchedule);
        }

        // If there's surplus payment beyond scheduled installments, apply directly to principal
        if (remainingPayment.compareTo(BigDecimal.ZERO) > 0) {
            totalPrincipalAllocated = totalPrincipalAllocated.add(remainingPayment);
        }

        // Update account outstanding principal
        BigDecimal newOutstanding = account.getOutstandingPrincipal().subtract(totalPrincipalAllocated);
        if (newOutstanding.compareTo(BigDecimal.ZERO) <= 0) {
            newOutstanding = BigDecimal.ZERO;
            account.setStatus(LoanAccountStatus.CLOSED);
            account.setClosedAt(LocalDateTime.now());

            LoanApplication application = account.getApplication();
            if (application != null) {
                application.setStatus(LoanApplicationStatus.CLOSED);
                loanApplicationRepository.save(application);
            }
        }
        account.setOutstandingPrincipal(newOutstanding.setScale(2, RoundingMode.HALF_UP));
        loanAccountRepository.save(account);

        // Record successful repayment
        LoanRepayment repayment = new LoanRepayment(
                account,
                paymentResult.getGatewayReference(),
                request.getIdempotencyKey(),
                request.getAmount(),
                LocalDateTime.now(),
                request.getPaymentMethod(),
                totalPrincipalAllocated,
                totalInterestAllocated,
                BigDecimal.ZERO,
                RepaymentStatus.SUCCESS,
                request.getNotes()
        );
        LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);

        String repaymentIdStr = savedRepayment.getId() != null ? savedRepayment.getId().toString() : "0";
        auditLogService.logAction("LoanRepayment", repaymentIdStr, "PROCESS_REPAYMENT",
                performedBy, String.format("Processed repayment amount %s for account %s (idempotency: %s)",
                        request.getAmount(), account.getAccountNumber(), request.getIdempotencyKey()), clientIp);

        return repaymentMapper.toResponse(savedRepayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentHistory(Long loanId) {
        LoanAccount account = findAccountByLoanId(loanId);
        return loanRepaymentRepository.findByLoanAccountIdOrderByPaymentDateDesc(account.getId()).stream()
                .map(repaymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RepaymentScheduleResponse getRepaymentSchedule(Long loanId) {
        LoanAccount account = findAccountByLoanId(loanId);
        List<RepaymentSchedule> schedules = repaymentScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(account.getId());

        RepaymentScheduleResponse response = new RepaymentScheduleResponse();
        response.setLoanAccountId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setPrincipalAmount(account.getPrincipalAmount());
        response.setAnnualInterestRate(account.getAnnualInterestRate());
        response.setTenureMonths(account.getTenureMonths());
        response.setMonthlyEmi(account.getMonthlyEmi());
        response.setOutstandingPrincipal(account.getOutstandingPrincipal());
        response.setStatus(account.getStatus());

        List<LoanScheduleItemResponse> items = schedules.stream()
                .map(s -> new LoanScheduleItemResponse(
                        s.getInstallmentNumber(),
                        s.getDueDate(),
                        s.getPrincipalAmount(),
                        s.getInterestAmount(),
                        s.getTotalEmi(),
                        s.getOutstandingPrincipal(),
                        s.getPaidAmount(),
                        s.getStatus(),
                        s.getPaidDate()
                ))
                .collect(Collectors.toList());

        response.setSchedules(items);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public OutstandingBalanceResponse getOutstandingBalance(Long loanId) {
        LoanAccount account = findAccountByLoanId(loanId);
        List<RepaymentSchedule> schedules = repaymentScheduleRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(account.getId());

        int remainingCount = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        LocalDate nextDueDate = null;
        BigDecimal nextDueAmount = BigDecimal.ZERO;

        for (RepaymentSchedule s : schedules) {
            totalPaid = totalPaid.add(s.getPaidAmount());
            if (s.getStatus() != ScheduleStatus.PAID) {
                remainingCount++;
                if (nextDueDate == null) {
                    nextDueDate = s.getDueDate();
                    nextDueAmount = s.getTotalEmi().subtract(s.getPaidAmount());
                }
            }
        }

        OutstandingBalanceResponse response = new OutstandingBalanceResponse();
        response.setLoanAccountId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setOriginalPrincipal(account.getPrincipalAmount());
        response.setOutstandingPrincipal(account.getOutstandingPrincipal());
        response.setMonthlyEmi(account.getMonthlyEmi());
        response.setRemainingInstallments(remainingCount);
        response.setTotalInstallments(account.getTenureMonths());
        response.setTotalPaidToDate(totalPaid);
        response.setNextDueDate(nextDueDate);
        response.setNextDueAmount(nextDueAmount);
        response.setStatus(account.getStatus());

        return response;
    }

    private LoanAccount findAccountByLoanId(Long loanId) {
        // Try finding by account ID first
        return loanAccountRepository.findById(loanId)
                .or(() -> loanAccountRepository.findByApplicationId(loanId))
                .orElseThrow(() -> new ResourceNotFoundException("LoanAccount", "id/applicationId", loanId));
    }
}
