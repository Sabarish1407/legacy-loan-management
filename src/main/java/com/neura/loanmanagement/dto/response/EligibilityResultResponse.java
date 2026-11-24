package com.neura.loanmanagement.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EligibilityResultResponse {

    private boolean eligible;
    private BigDecimal maxEligibleAmount;
    private BigDecimal recommendedInterestRate;
    private BigDecimal projectedMonthlyEmi;
    private Integer evaluatedCreditScore;
    private List<String> reasons = new ArrayList<>();
    private List<String> remarks = new ArrayList<>();

    public EligibilityResultResponse() {
    }

    public EligibilityResultResponse(boolean eligible, BigDecimal maxEligibleAmount,
                                   BigDecimal recommendedInterestRate, BigDecimal projectedMonthlyEmi,
                                   Integer evaluatedCreditScore) {
        this.eligible = eligible;
        this.maxEligibleAmount = maxEligibleAmount;
        this.recommendedInterestRate = recommendedInterestRate;
        this.projectedMonthlyEmi = projectedMonthlyEmi;
        this.evaluatedCreditScore = evaluatedCreditScore;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public BigDecimal getMaxEligibleAmount() {
        return maxEligibleAmount;
    }

    public void setMaxEligibleAmount(BigDecimal maxEligibleAmount) {
        this.maxEligibleAmount = maxEligibleAmount;
    }

    public BigDecimal getRecommendedInterestRate() {
        return recommendedInterestRate;
    }

    public void setRecommendedInterestRate(BigDecimal recommendedInterestRate) {
        this.recommendedInterestRate = recommendedInterestRate;
    }

    public BigDecimal getProjectedMonthlyEmi() {
        return projectedMonthlyEmi;
    }

    public void setProjectedMonthlyEmi(BigDecimal projectedMonthlyEmi) {
        this.projectedMonthlyEmi = projectedMonthlyEmi;
    }

    public Integer getEvaluatedCreditScore() {
        return evaluatedCreditScore;
    }

    public void setEvaluatedCreditScore(Integer evaluatedCreditScore) {
        this.evaluatedCreditScore = evaluatedCreditScore;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void addReason(String reason) {
        this.reasons.add(reason);
    }

    public List<String> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<String> remarks) {
        this.remarks = remarks;
    }

    public void addRemark(String remark) {
        this.remarks.add(remark);
    }
}
