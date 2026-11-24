package com.neura.loanmanagement.controller;

import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.UpdateCustomerRequest;
import com.neura.loanmanagement.dto.response.ApiResponse;
import com.neura.loanmanagement.dto.response.CustomerResponse;
import com.neura.loanmanagement.entity.enums.KycStatus;
import com.neura.loanmanagement.security.CustomerSecurityService;
import com.neura.loanmanagement.security.SecurityUtils;
import com.neura.loanmanagement.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerSecurityService customerSecurityService;

    public CustomerController(CustomerService customerService,
                              CustomerSecurityService customerSecurityService) {
        this.customerService = customerService;
        this.customerSecurityService = customerSecurityService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            HttpServletRequest httpRequest) {
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        CustomerResponse response = customerService.createCustomer(request, performedBy, clientIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        customerSecurityService.validateCustomerAccess(id);
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            HttpServletRequest httpRequest) {
        customerSecurityService.validateCustomerAccess(id);
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        CustomerResponse response = customerService.updateCustomer(id, request, performedBy, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Customer updated successfully", response));
    }

    @PatchMapping("/{id}/kyc")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateKycStatus(
            @PathVariable Long id,
            @RequestParam KycStatus status,
            HttpServletRequest httpRequest) {
        String performedBy = SecurityUtils.getCurrentUsername();
        String clientIp = httpRequest.getRemoteAddr();
        CustomerResponse response = customerService.updateKycStatus(id, status, performedBy, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("KYC status updated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
