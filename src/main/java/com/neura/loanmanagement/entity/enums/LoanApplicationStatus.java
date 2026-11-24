package com.neura.loanmanagement.entity.enums;

import java.util.EnumSet;
import java.util.Set;

public enum LoanApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    DISBURSED,
    CLOSED;

    public boolean canTransitionTo(LoanApplicationStatus target) {
        if (target == null || target == this) {
            return false;
        }
        switch (this) {
            case SUBMITTED:
                return target == UNDER_REVIEW || target == APPROVED || target == REJECTED;
            case UNDER_REVIEW:
                return target == APPROVED || target == REJECTED;
            case APPROVED:
                return target == DISBURSED || target == REJECTED;
            case DISBURSED:
                return target == CLOSED;
            case REJECTED:
            case CLOSED:
            default:
                return false;
        }
    }
}
