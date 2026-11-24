package com.neura.loanmanagement.mapper;

import com.neura.loanmanagement.dto.response.LoanRepaymentResponse;
import com.neura.loanmanagement.entity.LoanRepayment;
import org.springframework.stereotype.Component;

@Component
public class RepaymentMapper {

    public LoanRepaymentResponse toResponse(LoanRepayment repayment) {
        if (repayment == null) {
            return null;
        }
        LoanRepaymentResponse response = new LoanRepaymentResponse();
        response.setRepaymentId(repayment.getId());
        if (repayment.getLoanAccount() != null) {
            response.setLoanAccountId(repayment.getLoanAccount().getId());
            response.setAccountNumber(repayment.getLoanAccount().getAccountNumber());
            response.setRemainingOutstandingPrincipal(repayment.getLoanAccount().getOutstandingPrincipal());
            response.setLoanAccountStatus(repayment.getLoanAccount().getStatus());
        }
        response.setPaymentReference(repayment.getPaymentReference());
        response.setIdempotencyKey(repayment.getIdempotencyKey());
        response.setAmountPaid(repayment.getAmount());
        response.setPrincipalComponent(repayment.getPrincipalComponent());
        response.setInterestComponent(repayment.getInterestComponent());
        response.setLateFee(repayment.getLateFee());
        response.setStatus(repayment.getStatus());
        response.setPaymentMethod(repayment.getPaymentMethod());
        response.setPaymentDate(repayment.getPaymentDate());
        response.setNotes(repayment.getNotes());
        return response;
    }
}
