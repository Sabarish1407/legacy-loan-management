package com.neura.loanmanagement.entity;

import com.neura.loanmanagement.entity.enums.DisbursementStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "disbursements")
public class Disbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LoanApplication application;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_account_id", nullable = false, unique = true)
    private LoanAccount loanAccount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDateTime disbursementDate;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DisbursementStatus status = DisbursementStatus.SUCCESS;

    @Column(name = "reference_number", nullable = false, unique = true, length = 100)
    private String referenceNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Disbursement() {
    }

    public Disbursement(LoanApplication application, LoanAccount loanAccount, BigDecimal amount,
                        LocalDateTime disbursementDate, String accountNumber, String referenceNumber) {
        this.application = application;
        this.loanAccount = loanAccount;
        this.amount = amount;
        this.disbursementDate = disbursementDate;
        this.accountNumber = accountNumber;
        this.referenceNumber = referenceNumber;
        this.status = DisbursementStatus.SUCCESS;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.disbursementDate == null) {
            this.disbursementDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = DisbursementStatus.SUCCESS;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanApplication getApplication() {
        return application;
    }

    public void setApplication(LoanApplication application) {
        this.application = application;
    }

    public LoanAccount getLoanAccount() {
        return loanAccount;
    }

    public void setLoanAccount(LoanAccount loanAccount) {
        this.loanAccount = loanAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(LocalDateTime disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public DisbursementStatus getStatus() {
        return status;
    }

    public void setStatus(DisbursementStatus status) {
        this.status = status;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
