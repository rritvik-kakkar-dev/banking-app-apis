package com.banking.banking_app_apis.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionResponse {
    private String transactionId;

    private String transactionReference;

    private String transactionType;

    private BigDecimal amount;

    private String accountNumber;

    private String accountName;

    private String counterpartyAccountNumber;

    private String status;

    private LocalDateTime createdAt;
}
