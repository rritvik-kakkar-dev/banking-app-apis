package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.Budget;
import com.banking.banking_app_apis.entity.BudgetGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByBudgetGroup(BudgetGroup budgetGroup);

    List<Budget> findByBudgetGroupAndStartDateLessThanEqualAndEndDateGreaterThanEqual(BudgetGroup group,
                                                                                      LocalDate endDate,
                                                                                      LocalDate startDate);
}
