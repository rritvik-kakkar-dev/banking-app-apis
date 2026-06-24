package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.dto.CreateLoanRequest;
import com.banking.banking_app_apis.dto.LoanResponse;
import com.banking.banking_app_apis.entity.User;

public interface LoanService {

    LoanResponse createLoan(CreateLoanRequest request, User currentUser);

}
