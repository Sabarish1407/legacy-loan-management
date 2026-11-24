package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.LoanAccountStatus;

import java.math.BigDecimal;
import java.util.List;

public class RepaymentScheduleResponse {

    private Long loanAccountId;
    private String accountNumber;
    private BigDecimal principalAmount;
    private BigDecimal annualInterestRate;
    private Integer tenureMonths;
    private BigDecimal monthlyEmi;
    private BigDecimal outstandingPrincipal;
    private LoanAccountStatus status;
    private List<LoanScheduleItemResponse> schedules;

    public RepaymentScheduleResponse() {
    }

    public Long getLoanAccountId() {
        return loanAccountId;
    }

    public void setLoanAccountId(Long loanAccountId) {
        this.loanAccountId = loanAccountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(BigDecimal annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public LoanAccountStatus getStatus() {
        return status;
    }

    public void setStatus(LoanAccountStatus status) {
        this.status = status;
    }

    public List<LoanScheduleItemResponse> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<LoanScheduleItemResponse> schedules) {
        this.schedules = schedules;
    }
}
