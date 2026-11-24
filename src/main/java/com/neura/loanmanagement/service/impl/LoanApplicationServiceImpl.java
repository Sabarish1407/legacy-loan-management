package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.request.EligibilityCheckRequest;
import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;
import com.neura.loanmanagement.dto.response.LoanApplicationResponse;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.exception.InvalidStateTransitionException;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.mapper.LoanApplicationMapper;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.LoanApplicationService;
import com.neura.loanmanagement.service.LoanEligibilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationServiceImpl.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanApplicationMapper loanApplicationMapper;
    private final LoanEligibilityService loanEligibilityService;
    private final AuditLogService auditLogService;

    public LoanApplicationServiceImpl(LoanApplicationRepository loanApplicationRepository,
                                      CustomerRepository customerRepository,
                                      LoanApplicationMapper loanApplicationMapper,
                                      LoanEligibilityService loanEligibilityService,
                                      AuditLogService auditLogService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.loanApplicationMapper = loanApplicationMapper;
        this.loanEligibilityService = loanEligibilityService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public LoanApplicationResponse submitApplication(LoanApplicationRequest request, String performedBy, String clientIp) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        // Run eligibility pre-assessment
        EligibilityCheckRequest eligibilityRequest = new EligibilityCheckRequest(
                customer.getId(),
                request.getLoanType(),
                request.getRequestedAmount(),
                request.getTenureMonths(),
                request.getMonthlyIncome(),
                request.getEmploymentType(),
                customer.getCreditScore(),
                null
        );
        EligibilityResultResponse eligibility = loanEligibilityService.checkEligibility(eligibilityRequest);

        LoanApplication application = loanApplicationMapper.toEntity(request, customer);
        application.setApplicationDate(LocalDateTime.now());
        application.setStatus(LoanApplicationStatus.SUBMITTED);

        if (!eligibility.isEligible()) {
            application.setReviewNotes("Automated eligibility check flagged reasons: " + String.join("; ", eligibility.getReasons()));
        } else {
            application.setReviewNotes("Automated eligibility check passed. Recommended rate: " + eligibility.getRecommendedInterestRate() + "%");
        }

        LoanApplication saved = loanApplicationRepository.save(application);

        auditLogService.logAction("LoanApplication", saved.getId().toString(), "SUBMIT_APPLICATION",
                performedBy, "Submitted loan application for customer " + customer.getId() + " amount " + request.getRequestedAmount(), clientIp);

        return loanApplicationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplicationById(Long id) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));
        return loanApplicationMapper.toResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> getApplicationsByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }
        return loanApplicationRepository.findByCustomerId(customerId).stream()
                .map(loanApplicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LoanApplicationResponse approveApplication(Long id, LoanApprovalRequest request, String performedBy, String clientIp) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));

        LoanApplicationStatus currentStatus = application.getStatus();
        if (!currentStatus.canTransitionTo(LoanApplicationStatus.APPROVED)) {
            throw new InvalidStateTransitionException(currentStatus, LoanApplicationStatus.APPROVED);
        }

        application.setStatus(LoanApplicationStatus.APPROVED);
        application.setApprovedAmount(request != null && request.getApprovedAmount() != null
                ? request.getApprovedAmount() : application.getRequestedAmount());
        application.setApprovedTenureMonths(request != null && request.getApprovedTenureMonths() != null
                ? request.getApprovedTenureMonths() : application.getTenureMonths());
        application.setApprovedInterestRate(request != null && request.getApprovedInterestRate() != null
                ? request.getApprovedInterestRate() : application.getInterestRate());

        if (request != null && request.getReviewNotes() != null) {
            application.setReviewNotes(request.getReviewNotes());
        }

        LoanApplication updated = loanApplicationRepository.save(application);

        auditLogService.logAction("LoanApplication", updated.getId().toString(), "APPROVE_LOAN",
                performedBy, "Approved loan application " + id + " amount " + updated.getApprovedAmount(), clientIp);

        return loanApplicationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public LoanApplicationResponse rejectApplication(Long id, LoanRejectionRequest request, String performedBy, String clientIp) {
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", id));

        LoanApplicationStatus currentStatus = application.getStatus();
        if (!currentStatus.canTransitionTo(LoanApplicationStatus.REJECTED)) {
            throw new InvalidStateTransitionException(currentStatus, LoanApplicationStatus.REJECTED);
        }

        application.setStatus(LoanApplicationStatus.REJECTED);
        application.setRejectionReason(request != null && request.getReason() != null
                ? request.getReason() : "Rejected by loan underwriting committee");

        LoanApplication updated = loanApplicationRepository.save(application);

        auditLogService.logAction("LoanApplication", updated.getId().toString(), "REJECT_LOAN",
                performedBy, "Rejected loan application " + id + " reason: " + updated.getRejectionReason(), clientIp);

        return loanApplicationMapper.toResponse(updated);
    }
}
