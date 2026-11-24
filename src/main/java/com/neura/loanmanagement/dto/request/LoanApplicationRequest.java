package com.neura.loanmanagement.dto.request;

import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanType;
import com.neura.loanmanagement.validation.ValidTenure;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class LoanApplicationRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1000.00", message = "Requested amount must be at least 1,000")
    @DecimalMax(value = "50000000.00", message = "Requested amount cannot exceed 50,000,000")
    private BigDecimal requestedAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.00", message = "Interest rate cannot be negative")
    @DecimalMax(value = "36.00", message = "Interest rate cannot exceed 36%")
    private BigDecimal interestRate;

    @NotNull(message = "Tenure in months is required")
    @ValidTenure
    private Integer tenureMonths;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than zero")
    private BigDecimal monthlyIncome;

    public LoanApplicationRequest() {
    }

    public LoanApplicationRequest(Long customerId, LoanType loanType, BigDecimal requestedAmount,
                                  BigDecimal interestRate, Integer tenureMonths,
                                  EmploymentType employmentType, BigDecimal monthlyIncome) {
        this.customerId = customerId;
        this.loanType = loanType;
        this.requestedAmount = requestedAmount;
        this.interestRate = interestRate;
        this.tenureMonths = tenureMonths;
        this.employmentType = employmentType;
        this.monthlyIncome = monthlyIncome;
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

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }
}
