package com.neura.loanmanagement.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class LoanRejectionRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Rejection reason cannot exceed 500 characters")
    private String reason;

    public LoanRejectionRequest() {
    }

    public LoanRejectionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
