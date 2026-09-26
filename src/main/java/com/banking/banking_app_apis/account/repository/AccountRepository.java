package com.banking.banking_app_apis.account.repository;

import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Optional<Account> findByUser(User currentUser);

    Page<Account> findByUser(User user, Pageable pageable);

    Optional<Account> findByUserAndDefaultAccountTrueAndStatus(User user, AccountStatus status);

    Account findByUserAndDefaultAccountTrue(User user);

    List<Account> findAllByUserAndStatus(User user, AccountStatus status);

    List<Account> findAllByUser(User user);

}
