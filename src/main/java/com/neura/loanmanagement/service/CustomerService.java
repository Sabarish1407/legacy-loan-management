package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.UpdateCustomerRequest;
import com.neura.loanmanagement.dto.response.CustomerResponse;
import com.neura.loanmanagement.entity.enums.KycStatus;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request, String performedBy, String clientIp);

    CustomerResponse getCustomerById(Long id);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request, String performedBy, String clientIp);

    CustomerResponse updateKycStatus(Long id, KycStatus kycStatus, String performedBy, String clientIp);

    List<CustomerResponse> getAllCustomers();
}
