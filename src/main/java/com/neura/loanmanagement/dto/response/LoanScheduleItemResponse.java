package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.ScheduleStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanScheduleItemResponse {

    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalEmi;
    private BigDecimal outstandingPrincipal;
    private BigDecimal paidAmount;
    private ScheduleStatus status;
    private LocalDate paidDate;

    public LoanScheduleItemResponse() {
    }

    public LoanScheduleItemResponse(Integer installmentNumber, LocalDate dueDate, BigDecimal principalAmount,
                                    BigDecimal interestAmount, BigDecimal totalEmi, BigDecimal outstandingPrincipal,
                                    BigDecimal paidAmount, ScheduleStatus status, LocalDate paidDate) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.totalEmi = totalEmi;
        this.outstandingPrincipal = outstandingPrincipal;
        this.paidAmount = paidAmount;
        this.status = status;
        this.paidDate = paidDate;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getTotalEmi() {
        return totalEmi;
    }

    public void setTotalEmi(BigDecimal totalEmi) {
        this.totalEmi = totalEmi;
    }

    public BigDecimal getOutstandingPrincipal() {
        return outstandingPrincipal;
    }

    public void setOutstandingPrincipal(BigDecimal outstandingPrincipal) {
        this.outstandingPrincipal = outstandingPrincipal;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }
}
