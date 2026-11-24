package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.LoanAccount;
import com.neura.loanmanagement.entity.enums.LoanAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    Optional<LoanAccount> findByAccountNumber(String accountNumber);
    Optional<LoanAccount> findByApplicationId(Long applicationId);
    List<LoanAccount> findByCustomerId(Long customerId);
    List<LoanAccount> findByStatus(LoanAccountStatus status);

    @Query("SELECT COALESCE(SUM(la.outstandingPrincipal), 0) FROM LoanAccount la " +
           "WHERE la.customer.id = :customerId AND la.status = 'ACTIVE'")
    BigDecimal sumOutstandingPrincipalByCustomerId(@Param("customerId") Long customerId);
}
