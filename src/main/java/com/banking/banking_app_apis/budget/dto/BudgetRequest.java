package com.banking.banking_app_apis.budget.dto;

import com.banking.banking_app_apis.budget.entity.BudgetPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {

    private Long budgetGroupId;
    private Long categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String linkedAccountNumber;
    private Boolean alertAt80Percent;
}
