package com.banking.banking_app_apis.dto;

import com.banking.banking_app_apis.entity.BudgetGroupType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetGroupRequest {

    private String name;
    private BudgetGroupType type;
    private String partnerEmail;
}
