package com.banking.banking_app_apis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankResponse {

    private String responseCode;
    private String responseMessage;
    private AccountInfo accountInfo;
    private String transactionId;
    private String counterpartySource;
}
