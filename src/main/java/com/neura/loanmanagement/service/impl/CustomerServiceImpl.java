package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.request.UpdateCustomerRequest;
import com.neura.loanmanagement.dto.response.CustomerResponse;
import com.neura.loanmanagement.entity.AppUser;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.enums.KycStatus;
import com.neura.loanmanagement.entity.enums.UserRole;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.exception.ResourceNotFoundException;
import com.neura.loanmanagement.mapper.CustomerMapper;
import com.neura.loanmanagement.repository.AppUserRepository;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.service.AuditLogService;
import com.neura.loanmanagement.service.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final CustomerMapper customerMapper;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               AppUserRepository appUserRepository,
                               CustomerMapper customerMapper,
                               AuditLogService auditLogService,
                               PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
        this.customerMapper = customerMapper;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String performedBy, String clientIp) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException(String.format("Customer with email '%s' already exists", request.getEmail()));
        }
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessRuleException(String.format("Customer with National ID '%s' already exists", request.getNationalId()));
        }

        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        // Provision demo user account for the customer so they can authenticate
        String username = request.getEmail().toLowerCase();
        if (!appUserRepository.existsByUsername(username)) {
            AppUser appUser = new AppUser(
                    username,
                    passwordEncoder.encode("password"),
                    UserRole.CUSTOMER,
                    savedCustomer
            );
            appUserRepository.save(appUser);
        }

        auditLogService.logAction("Customer", savedCustomer.getId().toString(), "CREATE_CUSTOMER",
                performedBy, "Created customer with ID " + savedCustomer.getId(), clientIp);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request, String performedBy, String clientIp) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setEmploymentType(request.getEmploymentType());
        if (request.getKycStatus() != null) {
            customer.setKycStatus(request.getKycStatus());
        }

        Customer updated = customerRepository.save(customer);

        auditLogService.logAction("Customer", updated.getId().toString(), "UPDATE_CUSTOMER",
                performedBy, "Updated contact and financial details for customer " + id, clientIp);

        return customerMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public CustomerResponse updateKycStatus(Long id, KycStatus kycStatus, String performedBy, String clientIp) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        customer.setKycStatus(kycStatus);
        Customer updated = customerRepository.save(customer);

        auditLogService.logAction("Customer", updated.getId().toString(), "UPDATE_KYC",
                performedBy, "Updated KYC status to " + kycStatus, clientIp);

        return customerMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
