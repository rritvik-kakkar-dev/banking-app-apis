package com.banking.banking_app_apis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "loan_repayments")
@Builder
public class LoanRepayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    private Integer emiNumber;

    private BigDecimal amountPaid = BigDecimal.ZERO;

    private BigDecimal penaltyPaid = BigDecimal.ZERO;

    private LocalDate paymentDate;

    private BigDecimal principalComponent = BigDecimal.ZERO;

    private BigDecimal interestComponent = BigDecimal.ZERO;

    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String remarks;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
