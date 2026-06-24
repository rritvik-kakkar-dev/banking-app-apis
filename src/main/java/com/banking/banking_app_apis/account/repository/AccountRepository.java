package com.banking.banking_app_apis.account.repository;

import com.banking.banking_app_apis.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);
}
