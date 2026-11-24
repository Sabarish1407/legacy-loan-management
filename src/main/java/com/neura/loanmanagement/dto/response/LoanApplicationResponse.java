package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import com.neura.loanmanagement.entity.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplicationResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private EmploymentType employmentType;
    private BigDecimal monthlyIncome;
    private LocalDateTime applicationDate;
    private LoanApplicationStatus status;
    private String rejectionReason;
    private BigDecimal approvedAmount;
    private Integer approvedTenureMonths;
    private BigDecimal approvedInterestRate;
    private String reviewNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanApplicationResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(Integer tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public LoanApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(LoanApplicationStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
