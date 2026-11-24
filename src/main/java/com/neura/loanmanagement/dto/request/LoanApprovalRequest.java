package com.neura.loanmanagement.dto.request;

import com.neura.loanmanagement.validation.ValidTenure;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class LoanApprovalRequest {

    @DecimalMin(value = "1000.00", message = "Approved amount must be at least 1,000")
    private BigDecimal approvedAmount;

    @ValidTenure
    private Integer approvedTenureMonths;

    @DecimalMin(value = "0.00", message = "Approved interest rate cannot be negative")
    @DecimalMax(value = "36.00", message = "Approved interest rate cannot exceed 36%")
    private BigDecimal approvedInterestRate;

    @Size(max = 1000, message = "Review notes cannot exceed 1000 characters")
    private String reviewNotes;

    public LoanApprovalRequest() {
    }

    public LoanApprovalRequest(BigDecimal approvedAmount, Integer approvedTenureMonths,
                               BigDecimal approvedInterestRate, String reviewNotes) {
        this.approvedAmount = approvedAmount;
        this.approvedTenureMonths = approvedTenureMonths;
        this.approvedInterestRate = approvedInterestRate;
        this.reviewNotes = reviewNotes;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public Integer getApprovedTenureMonths() {
        return approvedTenureMonths;
    }

    public void setApprovedTenureMonths(Integer approvedTenureMonths) {
        this.approvedTenureMonths = approvedTenureMonths;
    }

    public BigDecimal getApprovedInterestRate() {
        return approvedInterestRate;
    }

    public void setApprovedInterestRate(BigDecimal approvedInterestRate) {
        this.approvedInterestRate = approvedInterestRate;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }
}
