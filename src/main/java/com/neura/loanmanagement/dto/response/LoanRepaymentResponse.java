package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.LoanAccountStatus;
import com.neura.loanmanagement.entity.enums.PaymentMethod;
import com.neura.loanmanagement.entity.enums.RepaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanRepaymentResponse {

    private Long repaymentId;
    private Long loanAccountId;
    private String accountNumber;
    private String paymentReference;
    private String idempotencyKey;
    private BigDecimal amountPaid;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal lateFee;
    private BigDecimal remainingOutstandingPrincipal;
    private RepaymentStatus status;
    private PaymentMethod paymentMethod;
    private LoanAccountStatus loanAccountStatus;
    private LocalDateTime paymentDate;
    private String notes;

    public LoanRepaymentResponse() {
    }

    public Long getRepaymentId() {
        return repaymentId;
    }

    public void setRepaymentId(Long repaymentId) {
        this.repaymentId = repaymentId;
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

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getPrincipalComponent() {
        return principalComponent;
    }

    public void setPrincipalComponent(BigDecimal principalComponent) {
        this.principalComponent = principalComponent;
    }

    public BigDecimal getInterestComponent() {
        return interestComponent;
    }

    public void setInterestComponent(BigDecimal interestComponent) {
        this.interestComponent = interestComponent;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public BigDecimal getRemainingOutstandingPrincipal() {
        return remainingOutstandingPrincipal;
    }

    public void setRemainingOutstandingPrincipal(BigDecimal remainingOutstandingPrincipal) {
        this.remainingOutstandingPrincipal = remainingOutstandingPrincipal;
    }

    public RepaymentStatus getStatus() {
        return status;
    }

    public void setStatus(RepaymentStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LoanAccountStatus getLoanAccountStatus() {
        return loanAccountStatus;
    }

    public void setLoanAccountStatus(LoanAccountStatus loanAccountStatus) {
        this.loanAccountStatus = loanAccountStatus;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
