package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.entity.*;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.exception.ValidationException;
import com.banking.banking_app_apis.repository.*;
import com.banking.banking_app_apis.service.BudgetService;
import com.banking.banking_app_apis.service.EmailService;
import com.banking.banking_app_apis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetGroupRepository budgetGroupRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;


    @Override
    public BudgetGroup createBudgetGroup(BudgetGroupRequest request, User currentUser) {
        User partnerUser = null;
        if(request.getType().equals(BudgetGroupType.COUPLE)) {
            if (!StringUtils.hasText(request.getPartnerEmail())) {
                throw new ValidationException("Partner email is required for couple budget groups.");
            }
            partnerUser = userRepository.findByEmail(request.getPartnerEmail()).orElseThrow(() -> new ResourceNotFoundException("No user found with partner email: " + request.getPartnerEmail()));
        }

        BudgetGroup budgetGroup = BudgetGroup.builder()
                .name(request.getName())
                .type(request.getType())
                .createdBy(currentUser)
                .partner(partnerUser)
                .active(true)
                .build();

        return budgetGroupRepository.save(budgetGroup);

    }

    @Override
    public void invitePartner(Long groupId, String partnerEmail, User currentUser) {

    }

    @Override
    public void acceptInvite(Long groupId, User currentUser) {

    }

    @Override
    public void splitCouple(Long groupId, User currentUser) {

    }

    @Override
    public Budget createBudget(BudgetRequest request, User currentUser) {

        BudgetGroup group = budgetGroupRepository.findById(request.getBudgetGroupId()).orElseThrow(() -> new ResourceNotFoundException("No Budget group found with this ID: " + request.getBudgetGroupId()));

        Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("No Category found with this ID: " + request.getCategoryId()));

        boolean isLinkedAccountUserExists = userRepository.existsByAccountNumber(request.getLinkedAccountNumber());
        if(!isLinkedAccountUserExists) {
            throw new ResourceNotFoundException("No User found with this account number: " + request.getLinkedAccountNumber());
        }

        User linkedAccountUser = userRepository.findByAccountNumber(request.getLinkedAccountNumber());

        LocalDate startDate;
        LocalDate endDate;

        if (request.getPeriod().equals(BudgetPeriod.MONTHLY)) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        } else if (request.getPeriod().equals(BudgetPeriod.ANNUAL)) {
            startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);        // Jan 1 current year
            endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);        // Dec 31 current year

        } else {
            if (request.getStartDate() == null) {
                throw new ValidationException("Start date is required for custom period budgets.");
            }
            if (request.getEndDate() == null) {
                throw new ValidationException("End date is required for custom period budgets.");
            }
            if (request.getEndDate().isBefore(request.getStartDate())) {
                throw new ValidationException("End date cannot be before start date.");
            }

            startDate = request.getStartDate();
            endDate = request.getEndDate();
        }

        Budget budget = Budget.builder()
                .budgetGroup(group)
                .category(category)
                .limitAmount(request.getLimitAmount())
                .period(request.getPeriod())
                .startDate(startDate)
                .endDate(endDate)
                .linkedAccount(linkedAccountUser)
                .alertAt80Percent(true)
                .build();

        return budgetRepository.save(budget);

    }

    @Override
    public Expense logExpense(ExpenseRequest request, User currentUser) {
        return null;
    }

    @Override
    public List<BudgetStatusResponse> getBudgetStatus(Long groupId, int year, int month) {
        return List.of();
    }

    @Override
    public List<BudgetStatusResponse> getAnnualBudgetStatus(Long groupId, int year) {
        return List.of();
    }

    @Override
    public List<BudgetStatusResponse> getCustomBudgetStatus(Long groupId, LocalDate from, LocalDate to) {
        return List.of();
    }

    @Override
    public List<Category> getCategories(User currentUser) {
        return categoryRepository.findByIsGlobalTrueOrCreatedBy(currentUser);
    }

    @Override
    public Category createCategory(CategoryRequest request, User currentUser) {
        Category category = Category.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .isGlobal(false)
                .createdBy(currentUser)
                .build();

        return categoryRepository.save(category);
    }
}
