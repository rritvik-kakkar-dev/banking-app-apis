package com.banking.banking_app_apis.budget.repository;

import com.banking.banking_app_apis.budget.entity.Category;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsGlobalTrue();

    List<Category> findByCreatedBy(User user);

    List<Category> findByIsGlobalTrueOrCreatedBy(User createdBy);
}
