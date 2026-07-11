package com.banking.banking_app_apis.user.controller;

import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.service.AccountService;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.user.dto.LoginRequest;
import com.banking.banking_app_apis.user.dto.UpdateUserRequest;
import com.banking.banking_app_apis.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Account Management APIs")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    AccountService accountService;


    @PostMapping
    @Operation(summary = "Create New User Account", description = "Create a new user and assigning an account ID")
    @ApiResponse(responseCode = "201", description = "Http Status 201 CREATED")
    public BankResponse register(@RequestBody UpdateUserRequest updateUserRequest) {
        return userService.register(updateUserRequest);
    }


    @PostMapping("/login")
    public BankResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }


    @GetMapping("/balanceEnquiry")
    // Swagger
    @Operation(summary = "Balance Enquiry", description = "Given an account number, check how much amount the user has")
    @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
    public BalanceResponse balanceEnquiry(@RequestParam String accountNumber) {
        EnquiryRequest enquiryRequest = new EnquiryRequest();
        enquiryRequest.setAccountNumber(accountNumber);
        return accountService.balanceEnquiry(enquiryRequest);
    }


    @GetMapping("/nameEnquiry")
    @Operation(summary = "Name Enquiry", description = "Given an account number, check the user Full Name")
    @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
    public AccountNameResponse nameEnquiry(@RequestBody EnquiryRequest enquiryRequest) {
        return accountService.nameEnquiry(enquiryRequest);
    }


    @PostMapping("/credit")
    @Operation(summary = "Credit Amount", description = "Given an account number and amount, credit the amount")
    @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
    public TransactionResponse creditAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return accountService.credit(creditDebitRequest);
    }


    @PostMapping("/transfer")
    @Operation(summary = "Transfer Amount", description = "Given a source account number, destination account number and amount, transfer the amount")
    @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
    public TransferResponse transfer(@RequestBody TransferRequest transferRequest) {
        return accountService.transfer(transferRequest);
    }

    @PostMapping("/debit")
    @Operation(summary = "Debit Amount", description = "Given an account number and amount, debit the amount")
    @ApiResponse(responseCode = "200", description = "Http Status 200 SUCCESS")
    public TransactionResponse debitAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return accountService.debit(creditDebitRequest);
    }
}
