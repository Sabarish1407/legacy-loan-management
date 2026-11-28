package com.neura.loanmanagement.controller;

import com.neura.loanmanagement.dto.request.LoanApplicationRequest;
import com.neura.loanmanagement.dto.request.LoanApprovalRequest;
import com.neura.loanmanagement.dto.request.LoanRejectionRequest;
import com.neura.loanmanagement.dto.response.ApiResponse;
import com.neura.loanmanagement.dto.response.LoanApplicationResponse;
import com.neura.loanmanagement.security.CustomerSecurityService;
import com.neura.loanmanagement.security.SecurityUtils;
import com.neura.loanmanagement.service.LoanApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final CustomerSecurityService customerSecurityService;

    public LoanApplicationController(LoanApplicationService loanApplicationService,
                                     CustomerSecurityService customerSecurityService) {
        this.loanApplicationService = loanApplicationService;
        this.customerSecurityService = customerSecurityService;
    }

    @PostMapping("/api/loans/applications")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> submitApplication(
            @Valid @RequestBody LoanApplicationRequest request,
            HttpServletRequest httpRequest) {
        customerSecurityService.validateCustomerAccess(request.getCustomerId());
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        LoanApplicationResponse response = loanApplicationService.submitApplication(request, performedBy, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Loan application submitted successfully", response));
    }

    @GetMapping("/api/loans/applications/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> getApplicationById(@PathVariable Long id) {
        customerSecurityService.validateLoanApplicationAccess(id);
        LoanApplicationResponse response = loanApplicationService.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/customers/{customerId}/loans")
    public ResponseEntity<ApiResponse<List<LoanApplicationResponse>>> getLoansByCustomerId(@PathVariable Long customerId) {
        customerSecurityService.validateCustomerAccess(customerId);
        List<LoanApplicationResponse> responses = loanApplicationService.getApplicationsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PatchMapping("/api/loans/applications/{id}/approve")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> approveApplication(
            @PathVariable Long id,
            @RequestBody(required = false) LoanApprovalRequest request,
            HttpServletRequest httpRequest) {
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        LoanApplicationResponse response = loanApplicationService.approveApplication(id, request, performedBy, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Loan application approved successfully", response));
    }

    @PatchMapping("/api/loans/applications/{id}/reject")
    public ResponseEntity<ApiResponse<LoanApplicationResponse>> rejectApplication(
            @PathVariable Long id,
            @RequestBody(required = false) LoanRejectionRequest request,
            HttpServletRequest httpRequest) {
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        LoanApplicationResponse response = loanApplicationService.rejectApplication(id, request, performedBy, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Loan application rejected", response));
    }
}
