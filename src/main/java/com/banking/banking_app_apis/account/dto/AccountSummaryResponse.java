package com.banking.banking_app_apis.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryResponse {

    private String accountName;
    private BigDecimal accountBalance;
    private String accountNumber;
}
