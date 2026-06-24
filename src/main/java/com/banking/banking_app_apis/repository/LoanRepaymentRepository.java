package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.Loan;
import com.banking.banking_app_apis.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoan(Loan loan);

    List<LoanRepayment> findByLoanId(Long loanId);

}
