package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.dto.response.LoanApplicationResponse;

import java.util.List;

public interface LoanApplicationService {

    LoanApplicationResponse submitApplication(LoanApplicationRequest request, String performedBy, String clientIp);

    LoanApplicationResponse getApplicationById(Long id);

    List<LoanApplicationResponse> getApplicationsByCustomerId(Long customerId);

    LoanApplicationResponse approveApplication(Long id, LoanApprovalRequest request, String performedBy, String clientIp);

    LoanApplicationResponse rejectApplication(Long id, LoanRejectionRequest request, String performedBy, String clientIp);
}
