package com.banking.banking_app_apis.account.dto;

import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountResponse {

    private Long id;
    private String accountNumber;
    private String accountName;
    private AccountType accountType;
    private BigDecimal balance;
    private CurrencyType currency;
    private AccountStatus status;
    private LocalDate createdAt;
}
