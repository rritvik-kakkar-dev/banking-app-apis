package com.banking.banking_app_apis.loan.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepaymentResponse {

    private Long id;

    private Integer emiNumber;

    private LocalDate paymentDate;

    private BigDecimal amountPaid;

    private BigDecimal principalPaid;

    private BigDecimal interestPaid;

    private BigDecimal penaltyPaid;

    private String paymentMode;

    private String transactionReference;

}