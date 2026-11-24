package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.LoanAccountStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OutstandingBalanceResponse {

    private Long loanAccountId;
    private String accountNumber;
    private BigDecimal originalPrincipal;
    private BigDecimal outstandingPrincipal;
    private BigDecimal monthlyEmi;
    private Integer remainingInstallments;
    private Integer totalInstallments;
    private BigDecimal totalPaidToDate;
    private LocalDate nextDueDate;
    private BigDecimal nextDueAmount;
    private LoanAccountStatus status;

    public OutstandingBalanceResponse() {
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

    public BigDecimal getOriginalPrincipal() {
        return originalPrincipal;
    }

    public void setOriginalPrincipal(BigDecimal originalPrincipal) {
        this.originalPrincipal = originalPrincipal;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public Integer getRemainingInstallments() {
        return remainingInstallments;
    }

    public void setRemainingInstallments(Integer remainingInstallments) {
        this.remainingInstallments = remainingInstallments;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public BigDecimal getTotalPaidToDate() {
        return totalPaidToDate;
    }

    public void setTotalPaidToDate(BigDecimal totalPaidToDate) {
        this.totalPaidToDate = totalPaidToDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public BigDecimal getNextDueAmount() {
        return nextDueAmount;
    }

    public void setNextDueAmount(BigDecimal nextDueAmount) {
        this.nextDueAmount = nextDueAmount;
    }

    public LoanAccountStatus getStatus() {
        return status;
    }

    public void setStatus(LoanAccountStatus status) {
        this.status = status;
    }
}
