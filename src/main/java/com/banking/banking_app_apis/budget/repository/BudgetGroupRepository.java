package com.banking.banking_app_apis.budget.repository;

import com.banking.banking_app_apis.budget.entity.BudgetGroup;
import com.banking.banking_app_apis.budget.entity.BudgetGroupType;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetGroupRepository extends JpaRepository<BudgetGroup, Long> {

    List<BudgetGroup> findByCreatedByOrPartner(User createdBy, User partner);

    boolean existsByCreatedByAndType(User createdBy, BudgetGroupType type);
}
