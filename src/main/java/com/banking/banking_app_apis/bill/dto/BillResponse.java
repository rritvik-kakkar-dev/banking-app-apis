package com.banking.banking_app_apis.bill.dto;

import com.banking.banking_app_apis.bill.entity.BillPeriod;
import com.banking.banking_app_apis.bill.entity.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long id;
    private String billName;
    private String billerName;
    private BillPeriod billPeriod;
    private BillStatus status;
    private BigDecimal amount;
    private LocalDate paidAt;
    private String accountNumber;
    private String accountName;
    private LocalDateTime createdAt;
}
