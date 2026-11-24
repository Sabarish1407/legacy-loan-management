package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.DisbursementStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanDisbursementResponse {

    private Long disbursementId;
    private Long loanApplicationId;
    private Long loanAccountId;
    private String accountNumber;
    private BigDecimal disbursementAmount;
    private LocalDateTime disbursementDate;
    private DisbursementStatus status;
    private String referenceNumber;
    private BigDecimal monthlyEmi;
    private Integer totalInstallments;
    private LocalDateTime createdAt;

    public LoanDisbursementResponse() {
    }

    public Long getDisbursementId() {
        return disbursementId;
    }

    public void setDisbursementId(Long disbursementId) {
        this.disbursementId = disbursementId;
    }

    public Long getLoanApplicationId() {
        return loanApplicationId;
    }

    public void setLoanApplicationId(Long loanApplicationId) {
        this.loanApplicationId = loanApplicationId;
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

    public BigDecimal getDisbursementAmount() {
        return disbursementAmount;
    }

    public void setDisbursementAmount(BigDecimal disbursementAmount) {
        this.disbursementAmount = disbursementAmount;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDateTime disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public DisbursementStatus getStatus() {
        return status;
    }

    public void setStatus(DisbursementStatus status) {
        this.status = status;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
