package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    List<LoanRepayment> findByLoanAccountIdOrderByPaymentDateDesc(Long loanAccountId);
    Optional<LoanRepayment> findByIdempotencyKey(String idempotencyKey);
    Optional<LoanRepayment> findByPaymentReference(String paymentReference);
    boolean existsByIdempotencyKey(String idempotencyKey);
}
