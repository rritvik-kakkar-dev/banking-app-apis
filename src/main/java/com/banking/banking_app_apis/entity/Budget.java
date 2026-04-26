package com.banking.banking_app_apis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "budget_group_id")
    private BudgetGroup budgetGroup;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    private BudgetPeriod period;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "linked_account_number", referencedColumnName = "accountNumber")
    private User linkedAccount;

    private boolean alertAt80Percent;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
