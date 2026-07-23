package com.banking.banking_app_apis.account.dto;

import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountRequest {

    private String accountName;

    private AccountType accountType;

    private CurrencyType currency;
}
