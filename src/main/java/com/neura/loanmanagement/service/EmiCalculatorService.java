package com.neura.loanmanagement.service;

import com.neura.loanmanagement.dto.response.LoanScheduleItemResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EmiCalculatorService {

    BigDecimal calculateMonthlyEmi(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths);

    List<LoanScheduleItemResponse> generateRepaymentSchedule(BigDecimal principal, BigDecimal annualInterestRate,
                                                             int tenureMonths, LocalDate startDate);
}
