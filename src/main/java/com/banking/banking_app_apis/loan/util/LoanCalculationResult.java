package com.banking.banking_app_apis.loan.util;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class LoanCalculationResult {

    private BigDecimal interestRate;

    private int totalEmis;

    private BigDecimal emiAmount;

    private BigDecimal totalInterest;

    private BigDecimal totalPayable;

    private LocalDate endDate;

    private LocalDate nextEmiDate;

}