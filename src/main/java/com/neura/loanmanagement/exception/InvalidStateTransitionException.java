package com.neura.loanmanagement.exception;

import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;

public class InvalidStateTransitionException extends RuntimeException {

    private final LoanApplicationStatus currentStatus;
    private final LoanApplicationStatus targetStatus;

    public InvalidStateTransitionException(LoanApplicationStatus currentStatus, LoanApplicationStatus targetStatus) {
        super(String.format("Cannot transition loan application from '%s' to '%s'", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public LoanApplicationStatus getCurrentStatus() {
        return currentStatus;
    }

    public LoanApplicationStatus getTargetStatus() {
        return targetStatus;
    }
}
