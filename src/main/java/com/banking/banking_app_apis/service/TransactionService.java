package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.dto.TransactionDto;
import com.banking.banking_app_apis.entity.Transaction;
import org.springframework.data.domain.Page;

public interface TransactionService {

    Transaction saveTransaction(TransactionDto transactionDto);

    Page<TransactionDto> transactionHistory(String accountNumber, int page, int limit, String sortBy, String sortOrder);
}
