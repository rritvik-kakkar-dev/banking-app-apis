package com.banking.banking_app_apis.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "loans")
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String loanSource;
    private String loanAccountNumber;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    private BigDecimal principal;
    private BigDecimal annualInterest;
    private Integer tenure;

    private Integer emiPaid = 0;
    private Integer emiPending = 0;

    @Enumerated(EnumType.STRING)
    private EmiFrequency emiFrequency;
    private BigDecimal emiAmount;

    private BigDecimal outstandingAmount = BigDecimal.ZERO;
    private BigDecimal totalInterest = BigDecimal.ZERO;
    private BigDecimal totalPayableAmount = BigDecimal.ZERO;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextEmiDate;
    private LocalDate closureDate;
    private BigDecimal preClosureAmount = BigDecimal.ZERO;

    private BigDecimal processingFee;
    private BigDecimal insuranceAmount;

    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private InterestType interestType;

    private Boolean autoDebitEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number")
    private String linkedAccount;

    @OneToMany(mappedBy = "loan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<LoanRepayment> repayments;

    @OneToMany(mappedBy = "loan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<LoanSchedule> schedules;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
