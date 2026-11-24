package com.neura.loanmanagement.unit;

import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.service.impl.EmiCalculatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EMI Calculator Service Unit Tests")
class EmiCalculatorServiceTest {

    private EmiCalculatorServiceImpl emiCalculator;

    @BeforeEach
    void setUp() {
        emiCalculator = new EmiCalculatorServiceImpl();
    }

    @Test
    @DisplayName("Should accurately calculate monthly EMI using standard reducing-balance formula")
    void testStandardEmiCalculation() {
        BigDecimal principal = new BigDecimal("100000.00");
        BigDecimal annualInterestRate = new BigDecimal("10.00"); // 10%
        int tenureMonths = 12;

        BigDecimal emi = emiCalculator.calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);

        assertNotNull(emi);
        // Formula check: For P=100000, r=10/1200, n=12: EMI is ~8791.59
        assertEquals(new BigDecimal("8791.59"), emi);
    }

    @Test
    @DisplayName("Should calculate monthly EMI correctly for 0% promotional interest rate")
    void testZeroPercentInterestEmi() {
        BigDecimal principal = new BigDecimal("120000.00");
        BigDecimal annualInterestRate = BigDecimal.ZERO;
        int tenureMonths = 12;

        BigDecimal emi = emiCalculator.calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);

        assertEquals(new BigDecimal("10000.00"), emi);
    }

    @Test
    @DisplayName("Should calculate EMI for boundary 1-month tenure")
    void testOneMonthTenure() {
        BigDecimal principal = new BigDecimal("50000.00");
        BigDecimal annualInterestRate = new BigDecimal("12.00"); // 1% for 1 month
        int tenureMonths = 1;

        BigDecimal emi = emiCalculator.calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);

        // 50000 + 1% interest (500) = 50500.00
        assertEquals(new BigDecimal("50500.00"), emi);
    }

    @Test
    @DisplayName("Should calculate EMI for large long-term mortgage loan")
    void testLargeMortgageLoan() {
        BigDecimal principal = new BigDecimal("5000000.00"); // 5 million
        BigDecimal annualInterestRate = new BigDecimal("7.50"); // 7.5%
        int tenureMonths = 240; // 20 years

        BigDecimal emi = emiCalculator.calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);

        assertNotNull(emi);
        assertTrue(emi.compareTo(BigDecimal.ZERO) > 0);
        // Expected ~40279.66
        assertEquals(new BigDecimal("40279.66"), emi);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when principal is zero or negative")
    void testInvalidPrincipal() {
        assertThrows(BusinessRuleException.class, () ->
                emiCalculator.calculateMonthlyEmi(BigDecimal.ZERO, new BigDecimal("10.00"), 12));

        assertThrows(BusinessRuleException.class, () ->
                emiCalculator.calculateMonthlyEmi(new BigDecimal("-1000.00"), new BigDecimal("10.00"), 12));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when interest rate is negative")
    void testNegativeInterestRate() {
        assertThrows(BusinessRuleException.class, () ->
                emiCalculator.calculateMonthlyEmi(new BigDecimal("10000.00"), new BigDecimal("-5.00"), 12));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when tenure is zero or negative")
    void testInvalidTenure() {
        assertThrows(BusinessRuleException.class, () ->
                emiCalculator.calculateMonthlyEmi(new BigDecimal("10000.00"), new BigDecimal("10.00"), 0));

        assertThrows(BusinessRuleException.class, () ->
                emiCalculator.calculateMonthlyEmi(new BigDecimal("10000.00"), new BigDecimal("10.00"), -12));
    }

    @Test
    @DisplayName("Should generate complete repayment schedule and verify total principal equals original amount")
    void testGenerateRepaymentSchedule() {
        BigDecimal principal = new BigDecimal("60000.00");
        BigDecimal annualInterestRate = new BigDecimal("12.00");
        int tenureMonths = 6;
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        List<LoanScheduleItemResponse> schedule = emiCalculator.generateRepaymentSchedule(
                principal, annualInterestRate, tenureMonths, startDate);

        assertNotNull(schedule);
        assertEquals(6, schedule.size());

        BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
        BigDecimal totalInterestPaid = BigDecimal.ZERO;

        for (int i = 0; i < schedule.size(); i++) {
            LoanScheduleItemResponse item = schedule.get(i);
            assertEquals(i + 1, item.getInstallmentNumber());
            assertEquals(startDate.plusMonths(i + 1), item.getDueDate());
            assertTrue(item.getPrincipalAmount().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(item.getInterestAmount().compareTo(BigDecimal.ZERO) > 0);

            totalPrincipalPaid = totalPrincipalPaid.add(item.getPrincipalAmount());
            totalInterestPaid = totalInterestPaid.add(item.getInterestAmount());
        }

        // Final installment outstanding principal must be 0
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), schedule.get(schedule.size() - 1).getOutstandingPrincipal());
        // Total principal paid across all installments must exactly match original principal
        assertEquals(principal.setScale(2, RoundingMode.HALF_UP), totalPrincipalPaid.setScale(2, RoundingMode.HALF_UP));
    }
}
