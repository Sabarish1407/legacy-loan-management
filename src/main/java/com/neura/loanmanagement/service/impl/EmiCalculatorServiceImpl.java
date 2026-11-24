package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;
import com.neura.loanmanagement.entity.enums.ScheduleStatus;
import com.neura.loanmanagement.exception.BusinessRuleException;
import com.neura.loanmanagement.service.EmiCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiCalculatorServiceImpl implements EmiCalculatorService {

    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");

    @Override
    public BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        validateInputs(principal, annualInterestRate, tenureMonths);

        if (annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        // monthly interest rate r = annualInterestRate / 1200
        BigDecimal r = annualInterestRate.divide(TWELVE_HUNDRED, MC);

        // (1 + r)^n
        BigDecimal onePlusR = BigDecimal.ONE.add(r, MC);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, MC);

        // numerator = P * r * (1 + r)^n
        BigDecimal numerator = principal.multiply(r, MC).multiply(onePlusRPowN, MC);

        // denominator = (1 + r)^n - 1
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE, MC);

        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal emi = numerator.divide(denominator, MC);
        return emi.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<LoanScheduleItemResponse> generateRepaymentSchedule(BigDecimal principal, BigDecimal annualInterestRate,
                                                                    int tenureMonths, LocalDate startDate) {
        validateInputs(principal, annualInterestRate, tenureMonths);
        if (startDate == null) {
            startDate = LocalDate.now();
        }

        BigDecimal emi = calculateMonthlyEmi(principal, annualInterestRate, tenureMonths);
        BigDecimal monthlyRate = annualInterestRate.compareTo(BigDecimal.ZERO) == 0 
                ? BigDecimal.ZERO 
                : annualInterestRate.divide(TWELVE_HUNDRED, MC);

        BigDecimal currentPrincipal = principal.setScale(2, RoundingMode.HALF_UP);
        List<LoanScheduleItemResponse> schedules = new ArrayList<>(tenureMonths);

        for (int i = 1; i <= tenureMonths; i++) {
            LocalDate dueDate = startDate.plusMonths(i);
            BigDecimal interestForMonth;
            BigDecimal principalForMonth;
            BigDecimal totalEmiForMonth;

            if (i == tenureMonths) {
                // Final installment absorbs any rounding residue
                interestForMonth = currentPrincipal.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
                principalForMonth = currentPrincipal;
                totalEmiForMonth = principalForMonth.add(interestForMonth);
                currentPrincipal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                interestForMonth = currentPrincipal.multiply(monthlyRate, MC).setScale(2, RoundingMode.HALF_UP);
                principalForMonth = emi.subtract(interestForMonth).setScale(2, RoundingMode.HALF_UP);

                // Guard against principal repayment exceeding remaining balance
                if (principalForMonth.compareTo(currentPrincipal) > 0) {
                    principalForMonth = currentPrincipal;
                }

                totalEmiForMonth = emi;
                currentPrincipal = currentPrincipal.subtract(principalForMonth).setScale(2, RoundingMode.HALF_UP);
            }

            schedules.add(new LoanScheduleItemResponse(
                    i,
                    dueDate,
                    principalForMonth,
                    interestForMonth,
                    totalEmiForMonth,
                    currentPrincipal,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    ScheduleStatus.PENDING,
                    null
            ));
        }

        return schedules;
    }

    private void validateInputs(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Principal amount must be greater than zero");
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Annual interest rate cannot be negative");
        }
        if (tenureMonths <= 0) {
            throw new BusinessRuleException("Tenure months must be greater than zero");
        }
    }
}
