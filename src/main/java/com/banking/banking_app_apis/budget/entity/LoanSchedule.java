package com.banking.banking_app_apis.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "loan_schedules")
@Builder
public class LoanSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private Integer emiNumber;

    private Integer month;

    private Integer year;

    private LocalDate dueDate;

    private BigDecimal emiAmount;

    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal balanceAfterPayment;

    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    private LocalDate paidDate;

    private String remarks;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
}
