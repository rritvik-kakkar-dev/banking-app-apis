package com.banking.banking_app_apis.controller;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public BankResponse createAccount(@RequestBody UserRequest userRequest) {
        return userService.createAccount(userRequest);
    }

    @GetMapping("/balanceEnquiry")
    public BankResponse balanceEnquiry(@RequestBody EnquiryRequest enquiryRequest) {
        return userService.balanceEnquiry(enquiryRequest);
    }

    @GetMapping("/nameEnquiry")
    public String nameEnquiry(@RequestBody EnquiryRequest enquiryRequest) {
        return userService.nameEnquiry(enquiryRequest);
    }


    @PostMapping("/credit")
    public BankResponse creditAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return userService.creditAmount(creditDebitRequest);
    }

    @PostMapping("/debit")
    public BankResponse debitAmount(@RequestBody CreditDebitRequest creditDebitRequest) {
        return userService.debitAmount(creditDebitRequest);
    }

    @PostMapping("/transfer")
    public BankResponse transfer(@RequestBody TransferRequest transferRequest) {
        return userService.transfer(transferRequest);
    }
}
