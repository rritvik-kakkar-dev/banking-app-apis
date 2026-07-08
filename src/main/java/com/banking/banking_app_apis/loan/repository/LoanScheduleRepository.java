package com.banking.banking_app_apis.loan.repository;

import com.banking.banking_app_apis.loan.entity.Loan;
import com.banking.banking_app_apis.loan.entity.LoanSchedule;
import com.banking.banking_app_apis.loan.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, Long> {

    List<LoanSchedule> findByLoan(Loan loan);

    List<LoanSchedule> findByLoanId(Long loanId);

    List<LoanSchedule> findByLoanIdAndStatus(Long loanId, PaymentStatus status);

    List<LoanSchedule> findByDueDateBetweenAndStatus(LocalDate from, LocalDate to, PaymentStatus status);
}
