package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
