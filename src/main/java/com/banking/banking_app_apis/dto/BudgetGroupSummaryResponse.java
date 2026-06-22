package com.banking.banking_app_apis.dto;

import com.banking.banking_app_apis.entity.BudgetGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGroupSummaryResponse {
    private Long id;
    private String name;
    private BudgetGroupType type;
    private BigDecimal groupLimitAmount;
    private boolean active;

    private String createdByName;
    private String createdByEmail;

    private String partnerName;   // null if SOLO or no partner
    private String partnerEmail;  // null if SOLO or no partner
}
