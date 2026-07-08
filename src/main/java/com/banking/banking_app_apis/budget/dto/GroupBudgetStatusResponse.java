package com.banking.banking_app_apis.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBudgetStatusResponse {
    private String groupName;
    private BigDecimal groupLimit;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private double percentUsed;
    private boolean alertTriggered;
}