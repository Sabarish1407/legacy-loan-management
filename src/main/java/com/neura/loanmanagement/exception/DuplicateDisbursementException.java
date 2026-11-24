package com.neura.loanmanagement.exception;

public class DuplicateDisbursementException extends RuntimeException {

    public DuplicateDisbursementException(Long loanId) {
        super(String.format("Loan with ID '%s' has already been disbursed", loanId));
    }

    public DuplicateDisbursementException(String message) {
        super(message);
    }
}
