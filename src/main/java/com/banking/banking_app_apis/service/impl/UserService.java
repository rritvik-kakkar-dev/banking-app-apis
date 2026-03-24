package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.*;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);

    BankResponse login(LoginDto loginDto);

    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    String nameEnquiry(EnquiryRequest enquiryRequest);

    BankResponse creditAmount(CreditDebitRequest creditDebitRequest);

    BankResponse debitAmount(CreditDebitRequest creditDebitRequest);

    BankResponse transfer(TransferRequest transferRequest);
}
