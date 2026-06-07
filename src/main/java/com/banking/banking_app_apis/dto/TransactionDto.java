package com.banking.banking_app_apis.dto;

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
public class TransactionDto {
    private String transactionReference;
    private String transactionType;
    private BigDecimal amount;
    private String accountNumber;
    private String counterpartySource;
    private String status;
    private LocalDateTime createdAt;
}
