package com.banking.banking_app_apis.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;

    private String transactionReference;

    private String transactionType;

    private BigDecimal amount;

    private String sourceAccountNumber;

    private String sourceAccountName;

    private String destinationAccountNumber;

    private String destinationAccountName;

    private String status;

    private LocalDateTime createdAt;
}
