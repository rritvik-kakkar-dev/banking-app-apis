package com.banking.banking_app_apis.account.dto;

import com.banking.banking_app_apis.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    private String accountNumber;

    private String accountName;

    private AccountType accountType;

    private BigDecimal openingBalance;
}
