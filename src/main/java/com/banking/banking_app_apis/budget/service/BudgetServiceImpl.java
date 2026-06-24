package com.banking.banking_app_apis.budget.service;

import com.banking.banking_app_apis.account.dto.CreditDebitRequest;
import com.banking.banking_app_apis.budget.dto.*;
import com.banking.banking_app_apis.budget.entity.*;
import com.banking.banking_app_apis.budget.repository.BudgetGroupRepository;
import com.banking.banking_app_apis.budget.repository.BudgetRepository;
import com.banking.banking_app_apis.budget.repository.CategoryRepository;
import com.banking.banking_app_apis.budget.repository.ExpenseRepository;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.notification.dto.EmailDetails;
import com.banking.banking_app_apis.notification.service.EmailService;
import com.banking.banking_app_apis.transaction.service.TransactionService;
import com.banking.banking_app_apis.user.repository.UserRepository;
import com.banking.banking_app_apis.user.service.UserService;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private TransactionService transactionService;


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
    public void deleteBudgetGroup(Long budgetGroupId, User currentUser) {

        BudgetGroup budgetGroup = budgetGroupRepository.findById(budgetGroupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget Group not found with ID: " + budgetGroupId));

        boolean isMember = budgetGroup.getCreatedBy().getId().equals(currentUser.getId())
                        || (budgetGroup.getPartner() != null
                        && budgetGroup.getPartner().getId().equals(currentUser.getId()));

        if (!isMember) {
            throw new ValidationException("You are not a member of this budget group.");
        }

        List<Budget> budgets = budgetRepository.findByBudgetGroup(budgetGroup);
        List<Expense> expenses = expenseRepository.findByBudgetIn(budgets);

        Map<Long, String> accountNumbersByBudgetId = budgets.stream()
                .collect(Collectors.toMap(
                        Budget::getId,
                        b -> b.getLinkedAccount().getAccountNumber()
                ));

        for (Expense expense : expenses) {
            CreditDebitRequest request = CreditDebitRequest.builder()
                    .accountNumber(accountNumbersByBudgetId.get(expense.getBudget().getId()))
                    .amount(expense.getAmount())
                    .source("Budget Group Deletion Refund")
                    .build();

            userService.creditAmount(request);
        }

        expenseRepository.deleteAll(expenses);
        budgetRepository.deleteAll(budgets);
        budgetGroupRepository.delete(budgetGroup);
    }

    @Override
    public void invitePartner(Long groupId, String partnerEmail, User currentUser) {

        BudgetGroup budgetGroup = budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                "group found with this ID: " + groupId));

        if(!budgetGroup.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ValidationException("Only the group creator can invite a partner");
        }

        if(!budgetGroup.getType().equals(BudgetGroupType.COUPLE)) {
            throw new ValidationException("Partner invite is only allowed for COUPLE budget groups");
        }

        if(budgetGroup.getPartner() != null) {
            throw new ValidationException("This group already has a partner");
        }

        User partner = userRepository.findByEmail(partnerEmail).orElseThrow(() -> new ResourceNotFoundException("No user found with email: " + partnerEmail));

        if (partner.getId().equals(currentUser.getId())) {
            throw new ValidationException("You cannot invite yourself as a partner.");
        }

        EmailDetails inviteEmail = EmailDetails.builder()
                .recipient(partnerEmail)
                .subject("Budget Group Invitation - Vaulta")
                .messageBody("Hi " + partner.getFirstName() + ",\n\n"
                        + currentUser.getFirstName() + " " + currentUser.getLastName()
                        + " has invited you to join their couple budget group: \""
                        + budgetGroup.getName() + "\".\n\n"
                        + "Your Group ID is: " + groupId + "\n"
                        + "Use this ID to accept the invite via the app.\n\n"
                        + "— Volta Banking")
                .build();

        emailService.sendEmailAlert(inviteEmail);

    }

    @Override
    public void acceptInvite(Long groupId, User currentUser) {

        BudgetGroup budgetGroup = budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                "group found with this ID: " + groupId));

        if(!budgetGroup.getType().equals(BudgetGroupType.COUPLE)) {
            throw new ValidationException("Partner invite is only allowed for COUPLE budget groups");
        }

        if(budgetGroup.getPartner() != null) {
            throw new ValidationException("This group already has a partner");
        }

        budgetGroup.setPartner(currentUser);
        budgetGroupRepository.save(budgetGroup);

        // Notify creator
        EmailDetails creatorNotification = EmailDetails.builder()
                .recipient(budgetGroup.getCreatedBy().getEmail())
                .subject("Partner Joined Your Budget Group — Vaulta")
                .messageBody("Hi " + budgetGroup.getCreatedBy().getFirstName() + ",\n\n"
                        + currentUser.getFirstName() + " " + currentUser.getLastName()
                        + " has accepted your invite and joined the budget group: \""
                        + budgetGroup.getName() + "\".\n\n"
                        + "— Vaulta Banking")
                .build();
        emailService.sendEmailAlert(creatorNotification);

        // Notify partner
        EmailDetails partnerNotification = EmailDetails.builder()
                .recipient(currentUser.getEmail())
                .subject("You Joined a Budget Group — Vaulta")
                .messageBody("Hi " + currentUser.getFirstName() + ",\n\n"
                        + "You have successfully joined the budget group: \""
                        + budgetGroup.getName() + "\".\n\n"
                        + "— Vaulta Banking")
                .build();
        emailService.sendEmailAlert(partnerNotification);
    }

    @Override
    public void splitCouple(Long groupId, User currentUser) {

        BudgetGroup budgetGroup = budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                "group found with this ID: " + groupId));

        boolean isCreator = budgetGroup.getCreatedBy().getId().equals(currentUser.getId());
        boolean isPartner = budgetGroup.getPartner() != null &&
                budgetGroup.getPartner().getId().equals(currentUser.getId());

        if (!isCreator && !isPartner) {
            throw new ValidationException("You are not a member of this budget group.");
        }

        if(!budgetGroup.getType().equals(BudgetGroupType.COUPLE)) {
            throw new ValidationException("Partner invite is only allowed for COUPLE budget groups");
        }

        if(budgetGroup.getPartner() == null) {
            throw new ValidationException("This group does not have a partner");
        }

        User formerPartner = budgetGroup.getPartner();

        budgetGroup.setPartner(null);
        budgetGroupRepository.save(budgetGroup);

        // Notify both users
        EmailDetails creatorNotification = EmailDetails.builder()
                .recipient(budgetGroup.getCreatedBy().getEmail())
                .subject("Budget Group Split — Vaulta")
                .messageBody("Hi " + budgetGroup.getCreatedBy().getFirstName() + ",\n\n"
                        + "Your couple budget group \"" + budgetGroup.getName()
                        + "\" has been split. All historical data is preserved.\n\n"
                        + "— Vaulta Banking")
                .build();
        emailService.sendEmailAlert(creatorNotification);

        EmailDetails partnerNotification = EmailDetails.builder()
                .recipient(formerPartner.getEmail())
                .subject("Budget Group Split — Vaulta")
                .messageBody("Hi " + formerPartner.getFirstName() + ",\n\n"
                        + "The couple budget group \"" + budgetGroup.getName()
                        + "\" has been split. All historical data is preserved.\n\n"
                        + "— Vaulta Banking")
                .build();
        emailService.sendEmailAlert(partnerNotification);
    }

    @Override
    public Budget createBudget(BudgetRequest request, User currentUser) {

        BudgetGroup group = budgetGroupRepository.findById(request.getBudgetGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("No Budget group found with this ID: " + request.getBudgetGroupId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("No Category found with this ID: " + request.getCategoryId()));

        boolean isLinkedAccountUserExists = userRepository.existsByAccountNumber(request.getLinkedAccountNumber());
        if(!isLinkedAccountUserExists) {
            throw new ResourceNotFoundException("No User found with this account number: " + request.getLinkedAccountNumber());
        }

        User linkedAccountUser = userRepository.findByAccountNumber(request.getLinkedAccountNumber());

        LocalDate[] dates = calculateBudgetDates(request);

        Budget budget = Budget.builder()
                .budgetGroup(group)
                .category(category)
                .limitAmount(request.getLimitAmount())
                .period(request.getPeriod())
                .startDate(dates[0])
                .endDate(dates[1])
                .linkedAccount(linkedAccountUser)
                .alertAt80Percent(true)
                .build();

        return budgetRepository.save(budget);

    }

    @Override
    public Budget updateBudget(Long budgetId, BudgetRequest request, User currentUser) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + budgetId));

        BudgetGroup group = budget.getBudgetGroup();
        boolean isMember = group.getCreatedBy().getId().equals(currentUser.getId())
                || (group.getPartner() != null && group.getPartner().getId().equals(currentUser.getId()));

        if (!isMember) {
            throw new ValidationException("You are not a member of this budget's group.");
        }

        // Update fields
        budget.setLimitAmount(request.getLimitAmount());
        budget.setAlertAt80Percent(request.getAlertAt80Percent() != null ? request.getAlertAt80Percent() : true);

        // If period or dates changed, recalculate — reuse the same logic as createBudget()
        LocalDate[] dates = calculateBudgetDates(request);
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(dates[0]);
        budget.setEndDate(dates[1]);

        return budgetRepository.save(budget);
    }

    @Override
    public void deleteBudget(Long budgetId, User currentUser) {

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Budget not found: " + budgetId));

        BudgetGroup group = budget.getBudgetGroup();

        boolean isMember = group.getCreatedBy().getId().equals(currentUser.getId())
                || (group.getPartner() != null
                && group.getPartner().getId().equals(currentUser.getId()));

        if (!isMember) {
            throw new ValidationException("You are not a member of this budget's group.");
        }

        String accountNumber = budget.getLinkedAccount().getAccountNumber();

        List<Expense> expenses = expenseRepository.findByBudget(budget);

        for (Expense expense : expenses) {
            userService.creditAmount(
                    CreditDebitRequest.builder()
                            .accountNumber(accountNumber)
                            .amount(expense.getAmount())
                            .source("Budget Deletion Refund")
                            .build()
            );
        }

        expenseRepository.deleteAll(expenses);
        budgetRepository.delete(budget);

    }


    @Override
    public Expense logExpense(ExpenseRequest request, User currentUser) {
        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + request.getBudgetId()));

        CreditDebitRequest debitRequest = CreditDebitRequest.builder()
                .accountNumber(budget.getLinkedAccount().getAccountNumber())
                .amount(request.getAmount())
                .destination(request.getDescription())
                .build();

        BankResponse debitResponse = userService.debitAmount(debitRequest);

        Expense expense = Expense.builder()
                .budget(budget)
                .loggedBy(currentUser)
                .amount(request.getAmount())
                .description(request.getDescription())
                .date(request.getDate())
                .transactionId(debitResponse.getTransactionId())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        checkBudgetAlerts(budget, currentUser);
        return savedExpense;
    }

    @Override
    public List<BudgetStatusResponse> getBudgetStatus(Long groupId, int year, int month) {

        BudgetGroup budgetGroup =
                budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                        "group found with this ID: " + groupId));

        List<Budget> budgets = budgetRepository.findByBudgetGroup(budgetGroup);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByBudgetBudgetGroupAndDateBetween(budgetGroup, startDate,
                endDate);

        Map<Long, BigDecimal> spendByCategory = expenses.stream().collect(
                Collectors.groupingBy(
                        e -> e.getBudget().getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                )
        );
        return mapToBudgetStatusResponse(budgets, spendByCategory);
    }

    @Override
    public List<BudgetStatusResponse> getAnnualBudgetStatus(Long groupId, int year) {
        BudgetGroup budgetGroup =
                budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                        "group found with this ID: " + groupId));

        List<Budget> budgets = budgetRepository.findByBudgetGroup(budgetGroup);

        LocalDate startDate = LocalDate.ofYearDay(year, 1);
        LocalDate endDate = startDate.withDayOfYear(startDate.lengthOfYear());

        List<Expense> expenses = expenseRepository.findByBudgetBudgetGroupAndDateBetween(budgetGroup, startDate,
                endDate);

        Map<Long, BigDecimal> spendByCategory = expenses.stream().collect(
                Collectors.groupingBy(
                        e -> e.getBudget().getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                )
        );

        return mapToBudgetStatusResponse(budgets, spendByCategory);
    }

    @Override
    public List<BudgetStatusResponse> getCustomBudgetStatus(Long groupId, LocalDate from, LocalDate to) {

        BudgetGroup budgetGroup =
                budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                        "group found with this ID: " + groupId));

        List<Budget> budgets = budgetRepository.findByBudgetGroup(budgetGroup);

        List<Expense> expenses = expenseRepository.findByBudgetBudgetGroupAndDateBetween(budgetGroup, from, to);

        Map<Long, BigDecimal> spendByCategory = expenses.stream().collect(
                Collectors.groupingBy(
                        e -> e.getBudget().getCategory().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                )
        );

        return mapToBudgetStatusResponse(budgets, spendByCategory);
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

    @Override
    public List<Expense> getExpenseHistory(Long budgetId, User currentUser) {
        Budget budget = budgetRepository.findById(budgetId).orElseThrow(() -> new ResourceNotFoundException("No Budget found with this ID: " + budgetId));
        List<Expense> expenses = expenseRepository.findByBudget(budget);

        return expenses.stream().sorted(Comparator.comparing(Expense::getDate).reversed()).toList();
    }

    @Override
    public List<BudgetGroupSummaryResponse> getMyBudgetGroups(User currentUser) {

        List<BudgetGroup> budgetGroups = budgetGroupRepository.findByCreatedByOrPartner(currentUser, currentUser);
        return mapToBudgetGroupSummaryResponse(budgetGroups);
    }

    @Override
    public BudgetGroup setGroupLimit(Long groupId, BigDecimal limitAmount, User currentUser) {
        BudgetGroup budgetGroup = budgetGroupRepository.findById(groupId).orElseThrow(() -> new ResourceNotFoundException("No Budget " +
                "group found with this ID: " + groupId));

        if (!budgetGroup.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ValidationException("Only the group creator can set limit to the budget group!");
        }

        if (limitAmount == null || limitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Limit amount must be greater than zero.");
        }

        budgetGroup.setGroupLimitAmount(limitAmount);
        return budgetGroupRepository.save(budgetGroup);
    }

    @Override
    public GroupBudgetStatusResponse getGroupBudgetStatus(Long groupId, int year, int month) {
        BudgetGroup budgetGroup = budgetGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("No Budget group found with this ID: " + groupId));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Expense> expenses = expenseRepository.findByBudgetBudgetGroupAndDateBetween(budgetGroup, startDate, endDate);

        BigDecimal totalSpent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal groupLimit = budgetGroup.getGroupLimitAmount();

        // No limit set yet — return status with null limit, 0% used
        if (groupLimit == null) {
            return GroupBudgetStatusResponse.builder()
                    .groupName(budgetGroup.getName())
                    .groupLimit(null)
                    .totalSpent(totalSpent)
                    .remaining(null)
                    .percentUsed(0.0)
                    .alertTriggered(false)
                    .build();
        }

        double percentUsed = groupLimit.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : totalSpent.divide(groupLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        return GroupBudgetStatusResponse.builder()
                .groupName(budgetGroup.getName())
                .groupLimit(groupLimit)
                .totalSpent(totalSpent)
                .remaining(groupLimit.subtract(totalSpent))
                .percentUsed(percentUsed)
                .alertTriggered(percentUsed >= 80.0)
                .build();
    }


    private void checkBudgetAlerts(Budget budget, User currentUser) {
        List<Expense> expenses = expenseRepository.findByBudgetAndDateBetween(budget, budget.getStartDate(),
                budget.getEndDate());

        BigDecimal totalSpent = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        double percentUsed = budget.getLimitAmount().compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : totalSpent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        
        if(percentUsed >= 100) {
             // Send Budget Exceeded email alert
            EmailDetails budgetExceededAlert = EmailDetails.builder()
                    .subject("BUDGET EXCEEDED")
                    .recipient(currentUser.getEmail())
                    .messageBody("You have exceeded you budget for " + budget.getCategory().getName() +" category!")
                    .build();

            emailService.sendEmailAlert(budgetExceededAlert);
        } else if (percentUsed >= 80 && budget.isAlertAt80Percent()) {
            // Send 80% budget used email alert
            EmailDetails budget80PercentUsedAlert = EmailDetails.builder()
                    .subject("BUDGET 80% USED")
                    .recipient(currentUser.getEmail())
                    .messageBody("You have 80% of your budget for " + budget.getCategory().getName() +" category!")
                    .build();

            emailService.sendEmailAlert(budget80PercentUsedAlert);
        }
    }

    private List<BudgetStatusResponse> mapToBudgetStatusResponse(List<Budget> budgets, Map<Long, BigDecimal> spendByCategory) {

        return budgets.stream()
                .map(budget -> {
                    BigDecimal spent = spendByCategory.getOrDefault(
                            budget.getCategory().getId(), BigDecimal.ZERO);
                    BigDecimal remaining = budget.getLimitAmount().subtract(spent);
                    double percentUsed = budget.getLimitAmount().compareTo(BigDecimal.ZERO) == 0
                            ? 0.0
                            : spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

                    return BudgetStatusResponse.builder()
                            .categoryName(budget.getCategory().getName())
                            .categoryIcon(budget.getCategory().getIcon())
                            .limit(budget.getLimitAmount())
                            .spent(spent)
                            .remaining(remaining)
                            .percentUsed(percentUsed)
                            .alertTriggered(percentUsed >= 80.0)
                            .budgetId(budget.getId())
                            .categoryColor(budget.getCategory().getColor())
                            .period(budget.getPeriod())
                            .linkedAccountNumber(budget.getLinkedAccount().getAccountNumber())
                            .build();
                })
                .collect(Collectors.toList());
    }
    private List<BudgetGroupSummaryResponse> mapToBudgetGroupSummaryResponse(List<BudgetGroup> budgetGroups) {

        return budgetGroups.stream()
                .map(budgetGroup -> {
                    return BudgetGroupSummaryResponse.builder()
                            .id(budgetGroup.getId())
                            .name(budgetGroup.getName())
                            .createdByName(buildFullName(budgetGroup.getCreatedBy()))
                            .createdByEmail(budgetGroup.getCreatedBy().getEmail())
                            .groupLimitAmount(budgetGroup.getGroupLimitAmount())
                            .type(budgetGroup.getType())
                            .partnerEmail(budgetGroup.getPartner() != null ? budgetGroup.getPartner().getEmail() : null)
                            .partnerName(budgetGroup.getPartner() != null ? buildFullName(budgetGroup.getPartner()) : null)
                            .active(budgetGroup.isActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String buildFullName(User user) {
        return Stream.of(user.getFirstName(), user.getLastName(), user.getOtherName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private LocalDate[] calculateBudgetDates(BudgetRequest request) {
        LocalDate startDate;
        LocalDate endDate;

        if (request.getPeriod().equals(BudgetPeriod.MONTHLY)) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        } else if (request.getPeriod().equals(BudgetPeriod.ANNUAL)) {
            startDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);
            endDate = LocalDate.of(LocalDate.now().getYear(), 12, 31);
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

        return new LocalDate[]{startDate, endDate};
    }

}
