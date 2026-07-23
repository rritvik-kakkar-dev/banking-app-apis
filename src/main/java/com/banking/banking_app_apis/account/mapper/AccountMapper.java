package com.banking.banking_app_apis.account.mapper;

import com.banking.banking_app_apis.account.dto.AccountSummaryResponse;
import com.banking.banking_app_apis.account.entity.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountMapper {

    public AccountSummaryResponse toSummaryResponse(Account account) {
        return AccountSummaryResponse.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
