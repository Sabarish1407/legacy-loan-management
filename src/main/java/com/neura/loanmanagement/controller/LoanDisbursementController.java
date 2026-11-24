package com.neura.loanmanagement.controller;

import com.neura.loanmanagement.dto.request.LoanDisbursementRequest;
import com.neura.loanmanagement.dto.response.ApiResponse;
import com.neura.loanmanagement.dto.response.LoanDisbursementResponse;
import com.neura.loanmanagement.security.SecurityUtils;
import com.neura.loanmanagement.service.LoanDisbursementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/loans")
public class LoanDisbursementController {

    private final LoanDisbursementService loanDisbursementService;

    public LoanDisbursementController(LoanDisbursementService loanDisbursementService) {
        this.loanDisbursementService = loanDisbursementService;
    }

    @PostMapping("/{loanId}/disburse")
    public ResponseEntity<ApiResponse<LoanDisbursementResponse>> disburseLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody(required = false) LoanDisbursementRequest request,
            HttpServletRequest httpRequest) {
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        LoanDisbursementResponse response = loanDisbursementService.disburseLoan(loanId, request, performedBy, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Loan disbursed successfully", response));
    }
}
