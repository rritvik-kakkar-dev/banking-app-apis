package com.banking.banking_app_apis.bill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillPaymentResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate paidAt;
    private BigDecimal pendingAmount;
    private LocalDateTime createdAt;

}
