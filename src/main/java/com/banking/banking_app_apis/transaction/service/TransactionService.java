package com.banking.banking_app_apis.transaction.service;

import com.banking.banking_app_apis.transaction.dto.TransactionResponse;
import com.banking.banking_app_apis.transaction.entity.Transaction;
import org.springframework.data.domain.Page;

public interface TransactionService {

    Transaction saveTransaction(TransactionResponse transactionResponse);

    Page<TransactionResponse> transactionHistory(String accountNumber, int page, int limit, String sortBy, String sortOrder);
}
