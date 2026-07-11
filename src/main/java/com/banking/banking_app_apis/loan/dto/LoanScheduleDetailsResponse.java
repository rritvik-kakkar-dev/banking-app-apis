package com.banking.banking_app_apis.loan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanScheduleDetailsResponse {

    private Long loanId;

    private String loanName;

    private String loanAccountNumber;

    private BigDecimal principal;

    private BigDecimal outstandingAmount;

    private BigDecimal emiAmount;

    private Integer emiPaid;

    private Integer emiPending;

    private List<LoanScheduleResponse> schedules;
}