package com.neura.loanmanagement.integration;

import com.neura.loanmanagement.LoanManagementApplication;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.repository.LoanApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = LoanManagementApplication.class)
@ActiveProfiles("test")
@DisplayName("Application Startup and Context Integration Tests")
class ApplicationStartupIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private LoanAccountRepository loanAccountRepository;

    @Test
    @DisplayName("Application context must start up cleanly without errors")
    void contextLoads() {
        assertNotNull(applicationContext);
        assertNotNull(customerRepository);
        assertNotNull(loanApplicationRepository);
        assertNotNull(loanAccountRepository);
    }

    @Test
    @DisplayName("H2 Test Database should be successfully connected with Flyway migrations applied")
    void testDatabaseConnectionAndFlywayApplied() throws SQLException {
        assertNotNull(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection);
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            assertTrue(dbProductName.equalsIgnoreCase("H2"), "Expected H2 test database, but got: " + dbProductName);
        }

        // Verify Flyway schema initialized tables and sample data is accessible
        long customerCount = customerRepository.count();
        assertTrue(customerCount >= 3, "Flyway V2 sample data should have seeded at least 3 customers");
    }
}
