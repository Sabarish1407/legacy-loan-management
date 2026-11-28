package com.neura.loanmanagement.controller;

import com.neura.loanmanagement.dto.request.EligibilityCheckRequest;
import com.neura.loanmanagement.dto.response.ApiResponse;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;
import com.neura.loanmanagement.security.CustomerSecurityService;
import com.neura.loanmanagement.service.LoanEligibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loans")
public class EligibilityController {

    private final LoanEligibilityService loanEligibilityService;
    private final CustomerSecurityService customerSecurityService;

    public EligibilityController(LoanEligibilityService loanEligibilityService,
                                 CustomerSecurityService customerSecurityService) {
        this.loanEligibilityService = loanEligibilityService;
        this.customerSecurityService = customerSecurityService;
    }

    @PostMapping("/eligibility")
    public ResponseEntity<ApiResponse<EligibilityResultResponse>> checkEligibility(
            @Valid @RequestBody EligibilityCheckRequest request) {
        if (request.getCustomerId() != null) {
            customerSecurityService.validateCustomerAccess(request.getCustomerId());
        }
        EligibilityResultResponse response = loanEligibilityService.checkEligibility(request);
        return ResponseEntity.ok(ApiResponse.ok("Eligibility assessment completed", response));
    }
}
