package com.banking.banking_app_apis.loan.dto;

import com.banking.banking_app_apis.loan.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayEmiResponse {

    private Long loanId;

    private Integer emiNumber;

    private Integer month;

    private Integer year;

    private BigDecimal amountPaid;

    private BigDecimal principalComponent;

    private BigDecimal interestComponent;

    private BigDecimal outstandingAmount;

    private Integer emiPaid;

    private Integer emiPending;

    private LocalDate paymentDate;

    private LocalDate nextEmiDate;

    private LoanStatus loanStatus;

    private String transactionReference;

    private String message;
}
