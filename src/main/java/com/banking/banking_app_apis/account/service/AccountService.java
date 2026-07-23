package com.banking.banking_app_apis.account.service;

import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;

public interface AccountService {

    Account createDefaultAccount(User user);

    CreateAccountResponse createAccount(CreateAccountRequest request, User user);

    Page<AccountSummaryResponse> getAccounts(User currentUser, int page, int limit, String sortBy, String sortOrder);

    AccountSummaryResponse getAccount(Long id, User currentUser);

    AccountSummaryResponse updateAccount(Long id, UpdateAccountRequest request, User currentUser);

    AccountResponse closeAccount(Long id, User currentUser);

    BalanceResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    AccountNameResponse nameEnquiry(EnquiryRequest enquiryRequest);

    TransactionResponse credit(CreditDebitRequest creditDebitRequest);

    TransactionResponse debit(CreditDebitRequest creditDebitRequest);

    TransferResponse transfer(TransferRequest transferRequest);
}
