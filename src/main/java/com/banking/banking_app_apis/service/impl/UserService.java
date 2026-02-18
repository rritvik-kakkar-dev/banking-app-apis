package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.BankResponse;
import com.banking.banking_app_apis.dto.EnquiryRequest;
import com.banking.banking_app_apis.dto.UserRequest;

public interface UserService {

    BankResponse createAccount(UserRequest userRequest);

    BankResponse balanceEnquiry(EnquiryRequest enquiryRequest);

    String nameEnquiry(EnquiryRequest enquiryRequest);
}
