package com.banking.banking_app_apis.loan.dto;

import com.banking.banking_app_apis.loan.entity.LoanStatus;
import com.banking.banking_app_apis.loan.entity.LoanType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanSummaryResponse {

    private Long id;

    private String name;

    private LoanType loanType;

    private BigDecimal principal;

    private BigDecimal outstandingAmount;

    private BigDecimal emiAmount;

    private LoanStatus status;

}
