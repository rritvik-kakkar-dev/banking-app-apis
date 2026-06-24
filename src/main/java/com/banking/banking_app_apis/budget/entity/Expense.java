package com.banking.banking_app_apis.budget.entity;

import com.banking.banking_app_apis.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "budget_id")
    private Budget budget;

    @ManyToOne
    @JoinColumn(name = "logged_by")
    private User loggedBy;

    private BigDecimal amount;
    private String description;
    private LocalDate date;

    private String transactionId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
