package com.banking.banking_app_apis.user.service;

import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.user.dto.LoginRequest;
import com.banking.banking_app_apis.user.dto.UpdateUserRequest;

public interface UserService {

    BankResponse login(LoginRequest loginRequest);

    BankResponse register(UpdateUserRequest updateUserRequest);

}
