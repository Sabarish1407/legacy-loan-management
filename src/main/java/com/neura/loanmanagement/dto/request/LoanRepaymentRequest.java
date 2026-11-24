package com.neura.loanmanagement.dto.request;

import com.neura.loanmanagement.entity.enums.PaymentMethod;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LoanRepaymentRequest {

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 100, message = "Idempotency key must not exceed 100 characters")
    private String idempotencyKey;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "1.00", message = "Payment amount must be at least 1.00")
    private BigDecimal amount;

    private PaymentMethod paymentMethod = PaymentMethod.SIMULATED_GATEWAY;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    public LoanRepaymentRequest() {
    }

    public LoanRepaymentRequest(String idempotencyKey, BigDecimal amount,
                                PaymentMethod paymentMethod, String notes) {
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.SIMULATED_GATEWAY;
        this.notes = notes;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
