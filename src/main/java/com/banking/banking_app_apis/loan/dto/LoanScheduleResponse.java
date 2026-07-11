package com.banking.banking_app_apis.loan.dto;

import com.banking.banking_app_apis.loan.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanScheduleResponse {

    private Integer emiNumber;

    private LocalDate dueDate;

    private BigDecimal emiAmount;

    private BigDecimal principalComponent;

    private BigDecimal interestComponent;

    private BigDecimal balanceAfterPayment;

    private BigDecimal penaltyAmount;

    private PaymentStatus status;

}