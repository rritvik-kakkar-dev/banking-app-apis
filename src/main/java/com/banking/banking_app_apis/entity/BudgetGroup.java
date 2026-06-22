package com.banking.banking_app_apis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget_groups")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private BudgetGroupType type;

    private BigDecimal groupLimitAmount;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    private User partner;

    private boolean active;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
