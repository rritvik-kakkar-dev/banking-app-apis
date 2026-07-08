package com.banking.banking_app_apis.common.dto;

import com.banking.banking_app_apis.account.dto.AccountSummaryResponse;
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
    private AccountSummaryResponse accountSummaryResponse;
    private String transactionId;
    private String counterpartySource;
}
