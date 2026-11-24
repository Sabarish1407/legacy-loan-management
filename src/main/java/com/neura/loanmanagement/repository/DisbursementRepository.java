package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.Disbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {
    Optional<Disbursement> findByApplicationId(Long applicationId);
    Optional<Disbursement> findByReferenceNumber(String referenceNumber);
    boolean existsByApplicationId(Long applicationId);
    boolean existsByLoanAccountId(Long loanAccountId);
}
