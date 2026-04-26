package com.banking.banking_app_apis.repository;

import com.banking.banking_app_apis.entity.BudgetGroup;
import com.banking.banking_app_apis.entity.BudgetGroupType;
import com.banking.banking_app_apis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetGroupRepository extends JpaRepository<BudgetGroup, Long> {

    List<BudgetGroup> findByCreatedByOrPartner(User createdBy, User partner);

    boolean existsByCreatedByAndType(User createdBy, BudgetGroupType type);
}
