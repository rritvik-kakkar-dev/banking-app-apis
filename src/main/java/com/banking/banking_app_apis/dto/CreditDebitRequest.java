package com.banking.banking_app_apis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDebitRequest {

    private String accountNumber;
    private BigDecimal amount;
    private String source;
    private String destination;
}
