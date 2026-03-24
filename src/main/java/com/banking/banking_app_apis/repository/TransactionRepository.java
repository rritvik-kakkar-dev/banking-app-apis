package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
}
