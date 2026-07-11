package com.banking.banking_app_apis.account.controller;

import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.service.AccountService;
import com.banking.banking_app_apis.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create New User Account", description = "Create a new user and assigning an account ID")
    @ApiResponse(responseCode = "201", description = "Http Status 201 CREATED")
    public Account createDefaultAccount(@AuthenticationPrincipal User currentUser) {
        return accountService.createDefaultAccount(currentUser);
    }
}
