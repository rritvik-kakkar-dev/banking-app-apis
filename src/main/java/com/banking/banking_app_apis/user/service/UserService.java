package com.banking.banking_app_apis.user.service;

import com.banking.banking_app_apis.account.dto.CreditDebitRequest;
import com.banking.banking_app_apis.account.dto.EnquiryRequest;
import com.banking.banking_app_apis.account.dto.TransferRequest;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.user.dto.LoginRequest;
import com.banking.banking_app_apis.user.dto.UpdateUserRequest;

public interface UserService {

    BankResponse createAccount(UpdateUserRequest updateUserRequest);

    BankResponse login(LoginRequest loginRequest);

    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    String nameEnquiry(EnquiryRequest enquiryRequest);

    BankResponse creditAmount(CreditDebitRequest creditDebitRequest);

    BankResponse debitAmount(CreditDebitRequest creditDebitRequest);

    BankResponse transfer(TransferRequest transferRequest);
}
