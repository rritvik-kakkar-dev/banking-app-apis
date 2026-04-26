package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.dto.TransactionDto;
import com.banking.banking_app_apis.entity.Transaction;

public interface TransactionService {

    void saveTransaction(TransactionDto transactionDto);
}
