package com.neura.loanmanagement.scheduler;

import com.neura.loanmanagement.entity.RepaymentSchedule;
import com.neura.loanmanagement.entity.enums.ScheduleStatus;
import com.neura.loanmanagement.repository.RepaymentScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverdueLoanScheduleChecker {

    private static final Logger log = LoggerFactory.getLogger(OverdueLoanScheduleChecker.class);

    private final RepaymentScheduleRepository repaymentScheduleRepository;

    public OverdueLoanScheduleChecker(RepaymentScheduleRepository repaymentScheduleRepository) {
        this.repaymentScheduleRepository = repaymentScheduleRepository;
    }

    @Scheduled(cron = "${loan.scheduler.overdue-check-cron:0 0 1 * * ?}")
    @Transactional
    public void checkAndFlagOverdueInstallments() {
        log.info("Running scheduled overdue loan installments check...");
        List<RepaymentSchedule> pendingOverdue = repaymentScheduleRepository
                .findByStatusAndDueDateBefore(ScheduleStatus.PENDING, LocalDate.now());

        for (RepaymentSchedule schedule : pendingOverdue) {
            schedule.setStatus(ScheduleStatus.OVERDUE);
            repaymentScheduleRepository.save(schedule);
            log.warn("Installment #{} for loan account #{} marked OVERDUE (due date: {})",
                    schedule.getInstallmentNumber(), schedule.getLoanAccount().getId(), schedule.getDueDate());
        }
    }
}
