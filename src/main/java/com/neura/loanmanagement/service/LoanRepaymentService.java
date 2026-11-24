package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.dto.response.LoanRepaymentResponse;
import com.neura.loanmanagement.dto.response.OutstandingBalanceResponse;
import com.neura.loanmanagement.dto.response.RepaymentScheduleResponse;

import java.util.List;

public interface LoanRepaymentService {

    LoanRepaymentResponse processRepayment(Long loanId, LoanRepaymentRequest request,
                                          String performedBy, String clientIp);

    List<LoanRepaymentResponse> getRepaymentHistory(Long loanId);

    RepaymentScheduleResponse getRepaymentSchedule(Long loanId);

    OutstandingBalanceResponse getOutstandingBalance(Long loanId);
}
