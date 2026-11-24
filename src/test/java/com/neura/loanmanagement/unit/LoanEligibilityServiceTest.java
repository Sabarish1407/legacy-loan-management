package com.neura.loanmanagement.unit;

import com.neura.loanmanagement.dto.request.EligibilityCheckRequest;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.service.impl.EmiCalculatorServiceImpl;
import com.neura.loanmanagement.service.impl.LoanEligibilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Loan Eligibility Service Unit Tests")
class LoanEligibilityServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanAccountRepository loanAccountRepository;

    private LoanEligibilityServiceImpl eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new LoanEligibilityServiceImpl(
                customerRepository,
                loanAccountRepository,
                new EmiCalculatorServiceImpl(),
                600,
                new BigDecimal("25000.00"),
                new BigDecimal("0.50"),
                new BigDecimal("10.0")
        );
    }

    @Test
    @DisplayName("Should approve eligible applicant with high credit score and income")
    void testEligibleApplicantPositive() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                1L,
                LoanType.PERSONAL,
                new BigDecimal("100000.00"),
                24,
                new BigDecimal("60000.00"),
                EmploymentType.SALARIED,
                780,
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertTrue(response.isEligible());
        assertTrue(response.getReasons().isEmpty());
        assertFalse(response.getRemarks().isEmpty());
        assertEquals(new BigDecimal("8.50"), response.getRecommendedInterestRate());
        assertEquals(new BigDecimal("600000.00"), response.getMaxEligibleAmount());
    }

    @Test
    @DisplayName("Should reject unemployed applicant regardless of income")
    void testRejectUnemployedApplicant() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                2L,
                LoanType.PERSONAL,
                new BigDecimal("50000.00"),
                12,
                new BigDecimal("50000.00"),
                EmploymentType.UNEMPLOYED,
                750,
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("Unemployed")));
    }

    @Test
    @DisplayName("Should reject applicant with income below minimum requirement")
    void testRejectLowIncome() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                3L,
                LoanType.PERSONAL,
                new BigDecimal("20000.00"),
                12,
                new BigDecimal("18000.00"), // < 25000
                EmploymentType.SALARIED,
                720,
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("below required minimum")));
    }

    @Test
    @DisplayName("Should reject applicant with credit score below minimum threshold")
    void testRejectLowCreditScore() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                4L,
                LoanType.VEHICLE,
                new BigDecimal("50000.00"),
                24,
                new BigDecimal("40000.00"),
                EmploymentType.SALARIED,
                550, // < 600
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("Credit score")));
    }

    @Test
    @DisplayName("Should reject applicant when total exposure exceeds 10x monthly income")
    void testRejectExcessiveExposure() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                5L,
                LoanType.HOME,
                new BigDecimal("500000.00"),
                60,
                new BigDecimal("30000.00"), // 10x is 300,000
                EmploymentType.BUSINESS,
                700,
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("Total loan exposure")));
    }

    @Test
    @DisplayName("Should reject applicant when projected EMI exceeds 50% of monthly income")
    void testRejectExcessiveEmiRatio() {
        EligibilityCheckRequest request = new EligibilityCheckRequest(
                6L,
                LoanType.PERSONAL,
                new BigDecimal("150000.00"),
                6, // Short tenure causes high EMI (~26k > 50% of 30k)
                new BigDecimal("30000.00"),
                EmploymentType.SALARIED,
                680,
                BigDecimal.ZERO
        );

        EligibilityResultResponse response = eligibilityService.checkEligibility(request);

        assertNotNull(response);
        assertFalse(response.isEligible());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.contains("exceeds 50%")));
    }

    @Test
    @DisplayName("Should apply tier rates correctly at credit score boundary values (600, 700, 750)")
    void testCreditScoreBoundaryTiers() {
        // At 750 -> 8.5%
        EligibilityCheckRequest req750 = new EligibilityCheckRequest(null, LoanType.PERSONAL,
                new BigDecimal("50000.00"), 36, new BigDecimal("40000.00"), EmploymentType.SALARIED, 750, BigDecimal.ZERO);
        assertEquals(new BigDecimal("8.50"), eligibilityService.checkEligibility(req750).getRecommendedInterestRate());

        // At 700 -> 10.0%
        EligibilityCheckRequest req700 = new EligibilityCheckRequest(null, LoanType.PERSONAL,
                new BigDecimal("50000.00"), 36, new BigDecimal("40000.00"), EmploymentType.SALARIED, 700, BigDecimal.ZERO);
        assertEquals(new BigDecimal("10.00"), eligibilityService.checkEligibility(req700).getRecommendedInterestRate());

        // At 650 -> 12.0%
        EligibilityCheckRequest req650 = new EligibilityCheckRequest(null, LoanType.PERSONAL,
                new BigDecimal("50000.00"), 36, new BigDecimal("40000.00"), EmploymentType.SALARIED, 650, BigDecimal.ZERO);
        assertEquals(new BigDecimal("12.00"), eligibilityService.checkEligibility(req650).getRecommendedInterestRate());

        // At 600 -> 14.5%
        EligibilityCheckRequest req600 = new EligibilityCheckRequest(null, LoanType.PERSONAL,
                new BigDecimal("50000.00"), 36, new BigDecimal("40000.00"), EmploymentType.SALARIED, 600, BigDecimal.ZERO);
        assertEquals(new BigDecimal("14.50"), eligibilityService.checkEligibility(req600).getRecommendedInterestRate());
    }
}
