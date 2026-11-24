package com.neura.loanmanagement.integration;

import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.KycStatus;
import com.neura.loanmanagement.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Customer Repository Integration Tests")
class CustomerRepositoryIT {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should persist and retrieve customer by email and national ID")
    void testCustomerPersistenceAndQueries() {
        Customer customer = new Customer(
                "Alex",
                "Taylor",
                "alex.taylor@example.com",
                "+1-555-4001",
                "NAT-ID-US-9999",
                "100 Main Street, Suite 400",
                LocalDate.of(1985, 3, 15),
                new BigDecimal("65000.00"),
                EmploymentType.SALARIED,
                710,
                KycStatus.VERIFIED
        );

        Customer saved = customerRepository.save(customer);
        assertNotNull(saved.getId());

        Optional<Customer> byEmail = customerRepository.findByEmail("alex.taylor@example.com");
        assertTrue(byEmail.isPresent());
        assertEquals("Alex", byEmail.get().getFirstName());
        assertEquals(710, byEmail.get().getCreditScore());

        Optional<Customer> byNatId = customerRepository.findByNationalId("NAT-ID-US-9999");
        assertTrue(byNatId.isPresent());
        assertEquals("Taylor", byNatId.get().getLastName());

        assertTrue(customerRepository.existsByEmail("alex.taylor@example.com"));
        assertTrue(customerRepository.existsByNationalId("NAT-ID-US-9999"));
        assertFalse(customerRepository.existsByEmail("nonexistent@example.com"));
    }
}
