package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.response.LoanDisbursementResponse;

public interface LoanDisbursementService {

    LoanDisbursementResponse disburseLoan(Long loanApplicationId, LoanDisbursementRequest request,
                                         String performedBy, String clientIp);
}
