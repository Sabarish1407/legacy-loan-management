package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.request.EligibilityCheckRequest;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;

public interface LoanEligibilityService {

    EligibilityResultResponse checkEligibility(EligibilityCheckRequest request);
}
