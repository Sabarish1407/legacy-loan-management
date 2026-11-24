package com.neura.loanmanagement.mapper;

import com.neura.loanmanagement.dto.response.LoanDisbursementResponse;
import com.neura.loanmanagement.entity.Disbursement;
import org.springframework.stereotype.Component;

@Component
public class DisbursementMapper {

    public LoanDisbursementResponse toResponse(Disbursement disbursement) {
        if (disbursement == null) {
            return null;
        }
        LoanDisbursementResponse response = new LoanDisbursementResponse();
        response.setDisbursementId(disbursement.getId());
        if (disbursement.getApplication() != null) {
            response.setLoanApplicationId(disbursement.getApplication().getId());
        }
        if (disbursement.getLoanAccount() != null) {
            response.setLoanAccountId(disbursement.getLoanAccount().getId());
            response.setMonthlyEmi(disbursement.getLoanAccount().getMonthlyEmi());
            response.setTotalInstallments(disbursement.getLoanAccount().getTenureMonths());
        }
        response.setAccountNumber(disbursement.getAccountNumber());
        response.setDisbursementAmount(disbursement.getAmount());
        response.setDisbursementDate(disbursement.getDisbursementDate());
        response.setStatus(disbursement.getStatus());
        response.setReferenceNumber(disbursement.getReferenceNumber());
        response.setCreatedAt(disbursement.getCreatedAt());
        return response;
    }
}
