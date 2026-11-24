package com.neura.loanmanagement.mapper;

import com.neura.loanmanagement.dto.request.CreateCustomerRequest;
import com.neura.loanmanagement.dto.response.CustomerResponse;
import com.neura.loanmanagement.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }
        return new Customer(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhone(),
                request.getNationalId(),
                request.getAddress(),
                request.getDateOfBirth(),
                request.getMonthlyIncome(),
                request.getEmploymentType(),
                request.getCreditScore() != null ? request.getCreditScore() : 700,
                null
        );
    }

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getNationalId(),
                customer.getAddress(),
                customer.getDateOfBirth(),
                customer.getMonthlyIncome(),
                customer.getEmploymentType(),
                customer.getCreditScore(),
                customer.getKycStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
