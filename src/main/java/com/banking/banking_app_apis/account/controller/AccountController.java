package com.banking.banking_app_apis.account.controller;

import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.service.AccountService;
import com.banking.banking_app_apis.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create New User Account", description = "Create a new user and assigning an account ID")
    @ApiResponse(responseCode = "201", description = "Http Status 201 CREATED")
    public Account createDefaultAccount(@AuthenticationPrincipal User currentUser) {
        return accountService.createDefaultAccount(currentUser);
    }


    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public CreateAccountResponse createAccount(@RequestBody CreateAccountRequest request, @AuthenticationPrincipal User currentUser) {
        return accountService.createAccount(request, currentUser);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AccountSummaryResponse>> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                accountService.getAccounts(
                        currentUser,
                        page,
                        limit,
                        sortBy,
                        sortOrder
                )
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public AccountSummaryResponse getAccount(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return accountService.getAccount(id, currentUser);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AccountSummaryResponse> updateAccount(@PathVariable Long id, @RequestBody UpdateAccountRequest request, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                accountService.updateAccount(id, request, currentUser)
        );
    }


    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                accountService.closeAccount(id, currentUser)
        );
    }
}
