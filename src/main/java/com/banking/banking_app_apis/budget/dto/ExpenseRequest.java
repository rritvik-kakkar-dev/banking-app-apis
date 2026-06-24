package com.banking.banking_app_apis.budget.dto;


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
public class ExpenseRequest {

    private Long budgetId;
    private BigDecimal amount;
    private String description;
    private LocalDate date;
}
