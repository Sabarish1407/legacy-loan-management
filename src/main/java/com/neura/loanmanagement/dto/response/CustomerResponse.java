package com.neura.loanmanagement.dto.response;

import com.neura.loanmanagement.entity.enums.EmploymentType;
import com.neura.loanmanagement.entity.enums.KycStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String nationalId;
    private String address;
    private LocalDate dateOfBirth;
    private BigDecimal monthlyIncome;
    private EmploymentType employmentType;
    private Integer creditScore;
    private KycStatus kycStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomerResponse() {
    }

    public CustomerResponse(Long id, String firstName, String lastName, String email, String phone,
                            String nationalId, String address, LocalDate dateOfBirth,
                            BigDecimal monthlyIncome, EmploymentType employmentType,
                            Integer creditScore, KycStatus kycStatus,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.nationalId = nationalId;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.creditScore = creditScore;
        this.kycStatus = kycStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public BigDecimal getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(BigDecimal monthlyIncome) {
        this.monthlyIncome = monthlyIncome;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(KycStatus kycStatus) {
        this.kycStatus = kycStatus;
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
