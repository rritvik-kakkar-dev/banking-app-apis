package com.banking.banking_app_apis.account.repository;

import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByUser(User currentUser);

    Page<Account> findByUser(User user, Pageable pageable);
}
