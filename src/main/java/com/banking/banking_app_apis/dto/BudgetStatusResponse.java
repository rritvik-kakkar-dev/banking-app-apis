package com.banking.banking_app_apis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BudgetStatusResponse {

    private String categoryName;
    private String categoryIcon;
    private BigDecimal limit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentUsed;
    private boolean alertTriggered;
}
