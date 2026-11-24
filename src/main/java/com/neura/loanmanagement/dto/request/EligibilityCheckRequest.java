package com.neura.loanmanagement.dto.request;

import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.validation.ValidTenure;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class EligibilityCheckRequest {

    private Long customerId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1000.00", message = "Requested amount must be at least 1,000")
    private BigDecimal requestedAmount;

    @NotNull(message = "Tenure in months is required")
    @ValidTenure
    private Integer tenureMonths;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than zero")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    private Integer creditScore;
    private BigDecimal existingOutstandingLoans;

    public EligibilityCheckRequest() {
    }

    public EligibilityCheckRequest(Long customerId, LoanType loanType, BigDecimal requestedAmount,
                                   Integer tenureMonths, BigDecimal monthlyIncome,
                                   EmploymentType employmentType, Integer creditScore,
                                   BigDecimal existingOutstandingLoans) {
        this.customerId = customerId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.tenureMonths = tenureMonths;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.creditScore = creditScore;
        this.existingOutstandingLoans = existingOutstandingLoans;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public BigDecimal getExistingOutstandingLoans() {
        return existingOutstandingLoans;
    }

    public void setExistingOutstandingLoans(BigDecimal existingOutstandingLoans) {
        this.existingOutstandingLoans = existingOutstandingLoans;
    }
}
