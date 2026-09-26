package com.banking.banking_app_apis.account.dto;

import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryResponse {

    private Long id;
    private String accountName;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private CurrencyType currency;
    private boolean defaultAccount;
    private AccountStatus status;
    private LocalDateTime createdAt;

}
