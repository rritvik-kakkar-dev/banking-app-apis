package com.banking.banking_app_apis.loan.repository;

import com.banking.banking_app_apis.loan.entity.Loan;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    Page<Loan> findByUser(User user, Pageable pageable);

}
