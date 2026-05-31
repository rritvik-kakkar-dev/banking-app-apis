package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.TransactionDto;
import com.banking.banking_app_apis.entity.Transaction;
import com.banking.banking_app_apis.repository.TransactionRepository;
import com.banking.banking_app_apis.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public Transaction saveTransaction(TransactionDto transactionDto) {
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionDto.getTransactionReference())
                .transactionType(transactionDto.getTransactionType())
                .accountNumber(transactionDto.getAccountNumber())
                .amount(transactionDto.getAmount())
                .status("SUCCESS")
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionDto> transactionHistory(
            String accountNumber,
            int page,
            int limit,
            String sortBy,
            String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<Transaction> transactions =
                transactionRepository.findByAccountNumber(accountNumber, pageable);

        return transactions.map(transaction -> {

            String counterpartyAccountNumber = null;

            if (transaction.getTransactionReference() != null) {

                List<Transaction> relatedTransactions =
                        transactionRepository.findByTransactionReference(
                                transaction.getTransactionReference());

                counterpartyAccountNumber = relatedTransactions.stream()
                        .filter(t -> !t.getAccountNumber()
                                .equals(transaction.getAccountNumber()))
                        .map(Transaction::getAccountNumber)
                        .findFirst()
                        .orElse(null);
            }

            return TransactionDto.builder()
                    .transactionReference(transaction.getTransactionReference())
                    .transactionType(transaction.getTransactionType())
                    .amount(transaction.getAmount())
                    .accountNumber(transaction.getAccountNumber())
                    .counterpartyAccountNumber(counterpartyAccountNumber)
                    .status(transaction.getStatus())
                    .createdAt(transaction.getCreatedAt())
                    .build();
        });
    }


}
