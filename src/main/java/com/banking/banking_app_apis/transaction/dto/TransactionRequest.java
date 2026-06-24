package com.banking.banking_app_apis.transaction.dto;

import com.banking.banking_app_apis.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionRequest {

    private String transactionReference;
    private String transactionType;
    private BigDecimal amount;
    private Account account;
    private String counterpartyAccountNumber;

}
