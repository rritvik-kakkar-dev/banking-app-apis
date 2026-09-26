package com.banking.banking_app_apis.bill.entity;

import com.banking.banking_app_apis.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String billName;
    private String billerName;
    private String description;

    private BigDecimal billAmount;
    private Integer monthlyDueDate;

    private LocalDate billPaidAt;

    @Enumerated(EnumType.STRING)
    private BillPeriod billPeriod;

    @Enumerated(EnumType.STRING)
    private BillStatus status;

    private Integer autopayEnabled;

    @OneToOne
    private Account account;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

}
