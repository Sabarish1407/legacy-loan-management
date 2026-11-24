package com.neura.loanmanagement.repository;

import com.neura.loanmanagement.entity.RepaymentSchedule;
import com.neura.loanmanagement.entity.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule, Long> {
    List<RepaymentSchedule> findByLoanAccountIdOrderByInstallmentNumberAsc(Long loanAccountId);
    List<RepaymentSchedule> findByLoanAccountIdAndStatus(Long loanAccountId, ScheduleStatus status);
    Optional<RepaymentSchedule> findByLoanAccountIdAndInstallmentNumber(Long loanAccountId, Integer installmentNumber);
    List<RepaymentSchedule> findByStatusAndDueDateBefore(ScheduleStatus status, LocalDate date);
}
