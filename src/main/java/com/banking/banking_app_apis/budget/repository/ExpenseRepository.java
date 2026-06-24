package com.banking.banking_app_apis.budget.repository;

import com.banking.banking_app_apis.budget.entity.Budget;
import com.banking.banking_app_apis.budget.entity.BudgetGroup;
import com.banking.banking_app_apis.budget.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByBudget(Budget budget);

    List<Expense> findByBudgetBudgetGroupAndDateBetween(BudgetGroup group, LocalDate startDate, LocalDate endDate);

    List<Expense> findByBudgetAndDateBetween(Budget budget, LocalDate startDate, LocalDate endDate);

    List<Expense> findByBudgetIn(List<Budget> budgets);

}
