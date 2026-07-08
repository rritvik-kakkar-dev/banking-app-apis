package com.banking.banking_app_apis.budget.dto;

import com.banking.banking_app_apis.budget.entity.BudgetGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGroupRequest {

    private String name;
    private BudgetGroupType type;
    private String partnerEmail;
    private BigDecimal groupLimitAmount;
}
