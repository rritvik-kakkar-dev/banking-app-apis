package com.banking.banking_app_apis.loan.repository;

import com.banking.banking_app_apis.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
