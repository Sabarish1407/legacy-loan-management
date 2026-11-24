package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.LoanApplication;
import com.neura.loanmanagement.entity.enums.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByCustomerId(Long customerId);
    List<LoanApplication> findByStatus(LoanApplicationStatus status);

    @Query("SELECT COALESCE(SUM(la.requestedAmount), 0) FROM LoanApplication la " +
           "WHERE la.customer.id = :customerId AND la.status IN ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'DISBURSED')")
    BigDecimal sumActiveLoanExposure(@Param("customerId") Long customerId);
}
