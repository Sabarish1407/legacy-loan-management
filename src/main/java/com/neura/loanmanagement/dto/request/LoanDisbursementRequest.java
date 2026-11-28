package com.neura.loanmanagement.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanDisbursementRequest {

    @DecimalMin(value = "1000.00", message = "Disbursement amount must be at least 1,000")
    private BigDecimal amount;

    private LocalDateTime disbursementDate;

    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;

    public LoanDisbursementRequest() {
    }

    public LoanDisbursementRequest(BigDecimal amount, LocalDateTime disbursementDate, String accountNumber) {
        this.amount = amount;
        this.disbursementDate = disbursementDate;
        this.accountNumber = accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDateTime disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
