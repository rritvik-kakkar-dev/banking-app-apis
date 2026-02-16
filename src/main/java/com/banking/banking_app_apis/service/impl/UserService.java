package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.BankResponse;
import com.banking.banking_app_apis.dto.UserRequest;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);
}
