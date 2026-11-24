package com.neura.loanmanagement.security;

import com.neura.loanmanagement.entity.LoanAccount;
import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.exception.UnauthorizedAccessException;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerSecurityService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanAccountRepository loanAccountRepository;

    public CustomerSecurityService(LoanApplicationRepository loanApplicationRepository,
                                   LoanAccountRepository loanAccountRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanAccountRepository = loanAccountRepository;
    }

    public void validateCustomerAccess(Long targetCustomerId) {
        Optional<UserPrincipal> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            UserPrincipal principal = currentUser.get();
            if (principal.isCustomer()) {
                if (principal.getCustomerId() == null || !principal.getCustomerId().equals(targetCustomerId)) {
                    throw new UnauthorizedAccessException("Access denied: Customers may only access their own customer data");
                }
            }
        }
    }

    public void validateLoanApplicationAccess(Long applicationId) {
        Optional<UserPrincipal> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            UserPrincipal principal = currentUser.get();
            if (principal.isCustomer()) {
                LoanApplication application = loanApplicationRepository.findById(applicationId)
                        .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", "id", applicationId));
                if (principal.getCustomerId() == null || !principal.getCustomerId().equals(application.getCustomer().getId())) {
                    throw new UnauthorizedAccessException("Access denied: Customers may only access their own loan applications");
                }
            }
        }
    }

    public void validateLoanAccountAccess(Long loanId) {
        Optional<UserPrincipal> currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            UserPrincipal principal = currentUser.get();
            if (principal.isCustomer()) {
                LoanAccount account = loanAccountRepository.findById(loanId)
                        .or(() -> loanAccountRepository.findByApplicationId(loanId))
                        .orElseThrow(() -> new ResourceNotFoundException("LoanAccount", "id/applicationId", loanId));
                if (principal.getCustomerId() == null || !principal.getCustomerId().equals(account.getCustomer().getId())) {
                    throw new UnauthorizedAccessException("Access denied: Customers may only access their own loan accounts and schedules");
                }
            }
        }
    }
}
