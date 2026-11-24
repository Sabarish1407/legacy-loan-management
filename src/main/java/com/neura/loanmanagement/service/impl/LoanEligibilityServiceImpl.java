package com.neura.loanmanagement.service.impl;

import com.neura.loanmanagement.dto.request.EligibilityCheckRequest;
import com.neura.loanmanagement.dto.response.EligibilityResultResponse;
import com.neura.loanmanagement.entity.Customer;
import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.repository.CustomerRepository;
import com.neura.loanmanagement.repository.LoanAccountRepository;
import com.neura.loanmanagement.service.EmiCalculatorService;
import com.neura.loanmanagement.service.LoanEligibilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LoanEligibilityServiceImpl implements LoanEligibilityService {

    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final EmiCalculatorService emiCalculatorService;

    private final int minCreditScore;
    private final BigDecimal minMonthlyIncome;
    private final BigDecimal maxEmiToIncomeRatio;
    private final BigDecimal maxTotalExposureRatio;

    public LoanEligibilityServiceImpl(
            CustomerRepository customerRepository,
            LoanAccountRepository loanAccountRepository,
            EmiCalculatorService emiCalculatorService,
            @Value("${loan.eligibility.min-credit-score:600}") int minCreditScore,
            @Value("${loan.eligibility.min-monthly-income:25000.00}") BigDecimal minMonthlyIncome,
            @Value("${loan.eligibility.max-emi-to-income-ratio:0.50}") BigDecimal maxEmiToIncomeRatio,
            @Value("${loan.eligibility.max-total-exposure-ratio:10.0}") BigDecimal maxTotalExposureRatio) {
        this.customerRepository = customerRepository;
        this.loanAccountRepository = loanAccountRepository;
        this.emiCalculatorService = emiCalculatorService;
        this.minCreditScore = minCreditScore;
        this.minMonthlyIncome = minMonthlyIncome;
        this.maxEmiToIncomeRatio = maxEmiToIncomeRatio;
        this.maxTotalExposureRatio = maxTotalExposureRatio;
    }

    @Override
    @Transactional(readOnly = true)
    public EligibilityResultResponse checkEligibility(EligibilityCheckRequest request) {
        int evaluatedCreditScore = resolveCreditScore(request);
        BigDecimal existingLoans = resolveExistingLoans(request);
        BigDecimal income = request.getMonthlyIncome();
        BigDecimal requestedAmount = request.getRequestedAmount();
        EmploymentType employment = request.getEmploymentType();

        EligibilityResultResponse result = new EligibilityResultResponse();
        result.setEvaluatedCreditScore(evaluatedCreditScore);

        boolean isEligible = true;

        // 1. Employment check
        if (employment == EmploymentType.UNEMPLOYED) {
            isEligible = false;
            result.addReason("Unemployed applicants are not eligible for credit facilities");
        }

        // 2. Minimum monthly income check
        if (income.compareTo(minMonthlyIncome) < 0) {
            isEligible = false;
            result.addReason(String.format("Monthly income (%s) is below required minimum of %s", income, minMonthlyIncome));
        }

        // 3. Credit score check
        if (evaluatedCreditScore < minCreditScore) {
            isEligible = false;
            result.addReason(String.format("Credit score (%d) is below required minimum threshold of %d", evaluatedCreditScore, minCreditScore));
        }

        // 4. Total exposure check (existing + requested <= 10x monthly income)
        BigDecimal maxAllowedExposure = income.multiply(maxTotalExposureRatio).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalExposure = existingLoans.add(requestedAmount);
        if (totalExposure.compareTo(maxAllowedExposure) > 0) {
            isEligible = false;
            result.addReason(String.format("Total loan exposure (%s) exceeds maximum permitted limit (%s)", totalExposure, maxAllowedExposure));
        }

        // 5. Projected EMI ratio check
        BigDecimal recommendedRate = determineInterestRate(evaluatedCreditScore);
        BigDecimal projectedEmi = emiCalculatorService.calculateMonthlyEmi(
                requestedAmount, recommendedRate, request.getTenureMonths());
        result.setProjectedMonthlyEmi(projectedEmi);
        result.setRecommendedInterestRate(recommendedRate);

        BigDecimal emiToIncomeRatio = projectedEmi.divide(income, 4, RoundingMode.HALF_UP);
        if (emiToIncomeRatio.compareTo(maxEmiToIncomeRatio) > 0) {
            isEligible = false;
            result.addReason(String.format("Projected monthly EMI (%s) exceeds %d%% of monthly income (ratio: %s)",
                    projectedEmi, maxEmiToIncomeRatio.multiply(BigDecimal.valueOf(100)).intValue(), emiToIncomeRatio));
        }

        // Calculate max eligible amount
        BigDecimal maxEligible = maxAllowedExposure.subtract(existingLoans);
        if (maxEligible.compareTo(BigDecimal.ZERO) < 0) {
            maxEligible = BigDecimal.ZERO;
        }
        result.setMaxEligibleAmount(maxEligible.setScale(2, RoundingMode.HALF_UP));

        result.setEligible(isEligible);
        if (isEligible) {
            result.addRemark("Applicant meets all underwriting criteria");
            result.addRemark(String.format("Qualified for preferred interest rate of %s%%", recommendedRate));
        }

        return result;
    }

    private int resolveCreditScore(EligibilityCheckRequest request) {
        if (request.getCreditScore() != null) {
            return request.getCreditScore();
        }
        if (request.getCustomerId() != null) {
            return customerRepository.findById(request.getCustomerId())
                    .map(Customer::getCreditScore)
                    .filter(s -> s != null && s > 0)
                    .orElse(700);
        }
        return 700;
    }

    private BigDecimal resolveExistingLoans(EligibilityCheckRequest request) {
        if (request.getExistingOutstandingLoans() != null) {
            return request.getExistingOutstandingLoans();
        }
        if (request.getCustomerId() != null) {
            return loanAccountRepository.sumOutstandingPrincipalByCustomerId(request.getCustomerId());
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal determineInterestRate(int creditScore) {
        if (creditScore >= 750) {
            return new BigDecimal("8.50");
        } else if (creditScore >= 700) {
            return new BigDecimal("10.00");
        } else if (creditScore >= 650) {
            return new BigDecimal("12.00");
        } else {
            return new BigDecimal("14.50");
        }
    }
}
