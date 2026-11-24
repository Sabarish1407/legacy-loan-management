package com.neura.loanmanagement.integration.credit;

public class CreditScoreResponse {

    private String nationalId;
    private Integer creditScore;
    private String riskRating;
    private String status;

    public CreditScoreResponse() {
    }

    public CreditScoreResponse(String nationalId, Integer creditScore, String riskRating, String status) {
        this.nationalId = nationalId;
        this.creditScore = creditScore;
        this.riskRating = riskRating;
        this.status = status;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String riskRating) {
        this.riskRating = riskRating;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
