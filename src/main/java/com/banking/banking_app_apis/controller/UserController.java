package com.banking.banking_app_apis.controller;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.service.impl.UserService;
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

    @PostMapping
    @Operation(
            summary = "Create New User Account",
            description = "Create a new user and assigning an account ID"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Http Status 201 CREATED"
    )
    public BankResponse createAccount(@RequestBody UserRequest userRequest) {
        return userService.createAccount(userRequest);
    }

    @PostMapping("/login")
    public BankResponse login(@RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @GetMapping("/balanceEnquiry")
    @Operation(
            summary = "Balance Enquiry",
            description = "Given an account number, check how much amount the user has"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    public BankResponse balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest) {
        return userService.balanceEnquiry(enquiryRequest);
    }

    @GetMapping("/nameEnquiry")
    @Operation(
            summary = "Name Enquiry",
            description = "Given an account number, check the user Full Name"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    public String nameEnquiry(@RequestBody EnquiryRequest enquiryRequest) {
        return userService.nameEnquiry(enquiryRequest);
    }


    @PostMapping("/credit")
    @Operation(
            summary = "Credit Amount",
            description = "Given an account number and amount, credit the amount"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    public BankResponse creditAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return userService.creditAmount(creditDebitRequest);
    }

    @PostMapping("/debit")
    @Operation(
            summary = "Debit Amount",
            description = "Given an account number and amount, debit the amount"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    public BankResponse debitAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return userService.debitAmount(creditDebitRequest);
    }

    @PostMapping("/transfer")
    @Operation(
            summary = "Transfer Amount",
            description = "Given a source account number, destination account number and amount, transfer the amount"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Http Status 200 SUCCESS"
    )
    public BankResponse transfer(@RequestBody TransferRequest transferRequest) {
        return userService.transfer(transferRequest);
    }
}
