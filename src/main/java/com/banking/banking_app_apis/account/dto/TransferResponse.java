package com.banking.banking_app_apis.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {

    private String transactionReference;

    private BigDecimal amount;

    private String sourceAccount;

    private String destinationAccount;

    private String transactionStatus;

    private LocalDateTime transferredAt;

}
