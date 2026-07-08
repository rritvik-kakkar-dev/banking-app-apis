package com.banking.banking_app_apis.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountNameResponse {

    private String accountNumber;

    private String accountHolderName;

}
