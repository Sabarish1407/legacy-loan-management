package com.neura.loanmanagement.integration.credit;

public class CreditScoreRequest {

    private String nationalId;
    private String email;

    public CreditScoreRequest() {
    }

    public CreditScoreRequest(String nationalId, String email) {
        this.nationalId = nationalId;
        this.email = email;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
