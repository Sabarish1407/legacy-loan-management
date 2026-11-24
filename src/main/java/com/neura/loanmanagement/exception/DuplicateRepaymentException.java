package com.neura.loanmanagement.exception;

public class DuplicateRepaymentException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateRepaymentException(String idempotencyKey) {
        super(String.format("Repayment with idempotency key '%s' has already been processed", idempotencyKey));
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
