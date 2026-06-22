package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.entity.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BudgetService {

    // Create a solo or couple budget group
    BudgetGroup createBudgetGroup(BudgetGroupRequest request, User currentUser);

    void deleteBudgetGroup(Long budgetGroupId, User currentUser);

    // Invite a partner to a couple group (sends email invite)
    void invitePartner(Long groupId, String partnerEmail, User currentUser);

    // Accept partner invite
    void acceptInvite(Long groupId, User currentUser);

    // Split couple group — unlinks partner, keeps data
    void splitCouple(Long groupId, User currentUser);

    // Create a budget for a specific category and period
    Budget createBudget(BudgetRequest request, User currentUser);

    Budget updateBudget(Long budgetId, BudgetRequest request, User currentUser);

    void deleteBudget(Long budgetId, User currentUser);

    // Log an expense — auto-debits linked account, creates transaction
    Expense logExpense(ExpenseRequest request, User currentUser);

    // Get budget status for a month
    List<BudgetStatusResponse> getBudgetStatus(Long groupId, int year, int month);

    // Get budget status for a full year
    List<BudgetStatusResponse> getAnnualBudgetStatus(Long groupId, int year);

    // Get budget status for custom date range
    List<BudgetStatusResponse> getCustomBudgetStatus(Long groupId, LocalDate from, LocalDate to);

    // Get all categories (global + user's custom)
    List<Category> getCategories(User currentUser);

    // Create a custom category
    Category createCategory(CategoryRequest request, User currentUser);

    List<Expense> getExpenseHistory(Long budgetId, User currentUser);

    List<BudgetGroupSummaryResponse> getMyBudgetGroups(User currentUser);

    BudgetGroup setGroupLimit(Long groupId, BigDecimal limitAmount, User currentUser);

    GroupBudgetStatusResponse getGroupBudgetStatus(Long groupId, int year, int month);
}
