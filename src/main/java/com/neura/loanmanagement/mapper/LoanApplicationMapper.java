package com.neura.loanmanagement.mapper;

import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.response.LoanApplicationResponse;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class LoanApplicationMapper {

    public LoanApplication toEntity(LoanApplicationRequest request, Customer customer) {
        if (request == null || customer == null) {
            return null;
        }
        return new LoanApplication(
                customer,
                request.getLoanType(),
                request.getRequestedAmount(),
                request.getInterestRate(),
                request.getTenureMonths(),
                request.getEmploymentType(),
                request.getMonthlyIncome()
        );
    }

    public LoanApplicationResponse toResponse(LoanApplication application) {
        if (application == null) {
            return null;
        }
        LoanApplicationResponse response = new LoanApplicationResponse();
        response.setId(application.getId());
        if (application.getCustomer() != null) {
            response.setCustomerId(application.getCustomer().getId());
            response.setCustomerName(application.getCustomer().getFirstName() + " " + application.getCustomer().getLastName());
        }
        response.setLoanType(application.getLoanType());
        response.setRequestedAmount(application.getRequestedAmount());
        response.setInterestRate(application.getInterestRate());
        response.setTenureMonths(application.getTenureMonths());
        response.setEmploymentType(application.getEmploymentType());
        response.setMonthlyIncome(application.getMonthlyIncome());
        response.setApplicationDate(application.getApplicationDate());
        response.setStatus(application.getStatus());
        response.setRejectionReason(application.getRejectionReason());
        response.setApprovedAmount(application.getApprovedAmount());
        response.setApprovedTenureMonths(application.getApprovedTenureMonths());
        response.setApprovedInterestRate(application.getApprovedInterestRate());
        response.setReviewNotes(application.getReviewNotes());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}
