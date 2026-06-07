package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TransactionRepository extends JpaRepository<Transaction, String>, PagingAndSortingRepository<Transaction, String> {

    Page<Transaction> findByAccountNumberAndCreatedAtBetween(
            String accountNumber,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<Transaction> findByAccountNumber(
            String accountNumber,
            Pageable pageable
    );

    List<Transaction> findByTransactionReferenceIn(Set<String> reference);
}
