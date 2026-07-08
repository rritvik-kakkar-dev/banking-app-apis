package com.banking.banking_app_apis.account.service;

import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.user.entity.User;

public interface AccountService {

    Account createDefaultAccount(User user);

    BalanceResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    AccountNameResponse nameEnquiry(EnquiryRequest enquiryRequest);

    TransactionResponse credit(CreditDebitRequest creditDebitRequest);

    TransactionResponse debit(CreditDebitRequest creditDebitRequest);

    TransferResponse transfer(TransferRequest transferRequest);
}
