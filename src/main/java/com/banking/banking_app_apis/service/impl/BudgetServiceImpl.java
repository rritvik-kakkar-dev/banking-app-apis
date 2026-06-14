package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.entity.*;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.exception.ValidationException;
import com.banking.banking_app_apis.repository.*;
import com.banking.banking_app_apis.service.BudgetService;
import com.banking.banking_app_apis.service.EmailService;
import com.banking.banking_app_apis.service.TransactionService;
import com.banking.banking_app_apis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Budget budget = budgetRepository.findById(request.getBudgetId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + request.getBudgetId()));

        CreditDebitRequest debitRequest = CreditDebitRequest.builder()
                .accountNumber(budget.getLinkedAccount().getAccountNumber())
                .amount(request.getAmount())
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

    private List<BudgetStatusResponse> mapToBudgetStatusResponse(
            List<Budget> budgets, Map<Long, BigDecimal> spendByCategory) {

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
                            .build();
                })
                .collect(Collectors.toList());
    }
}
