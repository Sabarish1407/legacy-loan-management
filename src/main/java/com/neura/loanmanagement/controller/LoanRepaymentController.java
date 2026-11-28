package com.neura.loanmanagement.controller;

import com.neura.loanmanagement.dto.request.LoanRepaymentRequest;
import com.neura.loanmanagement.dto.response.ApiResponse;
import com.neura.loanmanagement.dto.response.LoanRepaymentResponse;
import com.neura.loanmanagement.dto.response.OutstandingBalanceResponse;
import com.neura.loanmanagement.dto.response.RepaymentScheduleResponse;
import com.neura.loanmanagement.security.CustomerSecurityService;
import com.neura.loanmanagement.security.SecurityUtils;
import com.neura.loanmanagement.service.LoanRepaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanRepaymentController {

    private final LoanRepaymentService loanRepaymentService;
    private final CustomerSecurityService customerSecurityService;

    public LoanRepaymentController(LoanRepaymentService loanRepaymentService,
                                  CustomerSecurityService customerSecurityService) {
        this.loanRepaymentService = loanRepaymentService;
        this.customerSecurityService = customerSecurityService;
    }

    @PostMapping("/{loanId}/repayments")
    public ResponseEntity<ApiResponse<LoanRepaymentResponse>> processRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRepaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
            HttpServletRequest httpRequest) {
        customerSecurityService.validateLoanAccountAccess(loanId);
        if (idempotencyHeader != null && !idempotencyHeader.isBlank()) {
            request.setIdempotencyKey(idempotencyHeader);
        }
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        LoanRepaymentResponse response = loanRepaymentService.processRepayment(loanId, request, performedBy, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Repayment processed successfully", response));
    }

    @GetMapping("/{loanId}/repayments")
    public ResponseEntity<ApiResponse<List<LoanRepaymentResponse>>> getRepayments(@PathVariable Long loanId) {
        customerSecurityService.validateLoanAccountAccess(loanId);
        List<LoanRepaymentResponse> responses = loanRepaymentService.getRepaymentHistory(loanId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<ApiResponse<RepaymentScheduleResponse>> getRepaymentSchedule(@PathVariable Long loanId) {
        customerSecurityService.validateLoanAccountAccess(loanId);
        RepaymentScheduleResponse response = loanRepaymentService.getRepaymentSchedule(loanId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{loanId}/balance")
    public ResponseEntity<ApiResponse<OutstandingBalanceResponse>> getOutstandingBalance(@PathVariable Long loanId) {
        customerSecurityService.validateLoanAccountAccess(loanId);
        OutstandingBalanceResponse response = loanRepaymentService.getOutstandingBalance(loanId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
