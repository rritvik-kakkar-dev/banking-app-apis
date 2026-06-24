package com.banking.banking_app_apis.transaction.service;

import com.banking.banking_app_apis.transaction.dto.TransactionRequest;
import com.banking.banking_app_apis.transaction.dto.TransactionResponse;
import com.banking.banking_app_apis.transaction.entity.Transaction;
import com.banking.banking_app_apis.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Override
    public Transaction saveTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .transactionReference(request.getTransactionReference())
                .transactionType(request.getTransactionType())
                .accountNumber(request.getAccount().getAccountNumber())
                .amount(request.getAmount())
                .counterPartySource(request.getCounterpartyAccountNumber())
                .status("SUCCESS")
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Page<TransactionResponse> transactionHistory(
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

        Set<String> references = transactions.getContent().stream()
                .map(Transaction::getTransactionReference)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> counterpartyMap = transactionRepository
                .findByTransactionReferenceIn(references)
                .stream()
                .filter(t -> !t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toMap(
                        Transaction::getTransactionReference,
                        Transaction::getAccountNumber,
                        (existing, replacement) -> existing
                ));

        return transactions.map(transaction ->
                TransactionResponse.builder()
                        .transactionId(transaction.getTransactionId())
                        .transactionReference(transaction.getTransactionReference())
                        .transactionType(transaction.getTransactionType())
                        .amount(transaction.getAmount())
                        .accountNumber(transaction.getAccountNumber())
                        .counterpartyAccountNumber(
                                counterpartyMap.get(transaction.getTransactionReference())
                        )
                        .status(transaction.getStatus())
                        .createdAt(transaction.getCreatedAt())
                        .build()
        );
    }


}
