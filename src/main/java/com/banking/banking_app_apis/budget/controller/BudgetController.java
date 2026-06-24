package com.banking.banking_app_apis.budget.controller;

import com.banking.banking_app_apis.budget.dto.*;
import com.banking.banking_app_apis.budget.entity.Budget;
import com.banking.banking_app_apis.budget.entity.BudgetGroup;
import com.banking.banking_app_apis.budget.entity.Category;
import com.banking.banking_app_apis.budget.entity.Expense;
import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.budget.service.BudgetService;
import com.banking.banking_app_apis.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budget")
@AllArgsConstructor
@Tag(name = "Budget APIs")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping("/groups")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public BudgetGroup createBudgetGroup(@RequestBody BudgetGroupRequest request,
                                         @AuthenticationPrincipal User currentUser) {
        return budgetService.createBudgetGroup(request, currentUser);
    }


    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void deleteBudgetGroup(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudgetGroup(id, currentUser);
    }


    @PostMapping("/groups/{id}/invite")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void invitePartner(@PathVariable Long id, @RequestParam String partnerEmail, @AuthenticationPrincipal User currentUser) {
        budgetService.invitePartner(id, partnerEmail, currentUser);
    }


    @PostMapping("/groups/{id}/accept")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void acceptInvite(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetService.acceptInvite(id, currentUser);
    }


    @PostMapping("/groups/{id}/split")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void splitCouple(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetService.splitCouple(id, currentUser);
    }


    @GetMapping("/categories")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Category> getCategories(@AuthenticationPrincipal User currentUser) {
        return budgetService.getCategories(currentUser);
    }


    @PostMapping("/categories")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Category createCategory(@RequestBody CategoryRequest request, @AuthenticationPrincipal User currentUser) {
        return budgetService.createCategory(request, currentUser);
    }


    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Budget createBudget(@RequestBody BudgetRequest request, @AuthenticationPrincipal User currentUser) {
        return budgetService.createBudget(request, currentUser);
    }


    @GetMapping("/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<BudgetStatusResponse> getBudgetStatus(@RequestParam Long groupId, @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month,
                                                      @RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        if (from != null && to != null) {
            return budgetService.getCustomBudgetStatus(groupId, from, to);
        } else if (year != null && month != null) {
            return budgetService.getBudgetStatus(groupId, year, month);
        } else if (year != null) {
            return budgetService.getAnnualBudgetStatus(groupId, year);
        } else {
            throw new ValidationException("Provide either year+month, year, or from+to");
        }
    }


    @PostMapping("/expenses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Expense logExpense(@RequestBody ExpenseRequest request, @AuthenticationPrincipal User currentUser) {
        return budgetService.logExpense(request, currentUser);
    }


    @GetMapping("/expenses")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Expense> expenseHistory(@RequestParam Long budgetId, @AuthenticationPrincipal User currentUser) {
        return budgetService.getExpenseHistory(budgetId, currentUser);
    }


    @GetMapping("/groups/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<BudgetGroupSummaryResponse> getMyBudgetGroups(@AuthenticationPrincipal User currentUser) {
        return budgetService.getMyBudgetGroups(currentUser);
    }


    @PutMapping("/groups/{id}/limit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public BudgetGroup setGroupLimit(@PathVariable Long id,
                                     @RequestParam BigDecimal limitAmount,
                                     @AuthenticationPrincipal User currentUser) {
        return budgetService.setGroupLimit(id, limitAmount, currentUser);
    }


    @GetMapping("/groups/{id}/group-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public GroupBudgetStatusResponse getGroupBudgetStatus(@PathVariable Long id,
                                                          @RequestParam int year,
                                                          @RequestParam int month,
                                                          @AuthenticationPrincipal User currentUser) {
        return budgetService.getGroupBudgetStatus(id, year, month);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Budget updateBudget(@PathVariable Long id,
                               @RequestBody BudgetRequest request,
                               @AuthenticationPrincipal User currentUser) {
        return budgetService.updateBudget(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public void deleteBudget(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        budgetService.deleteBudget(id, currentUser);
    }
}
