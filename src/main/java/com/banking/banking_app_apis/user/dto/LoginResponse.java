package com.banking.banking_app_apis.user.dto;

import com.banking.banking_app_apis.account.dto.AccountSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String message;
    private String accessToken;
    private List<AccountSummaryResponse> accounts;
}
