package com.banking.banking_app_apis.loan.dto;

import com.banking.banking_app_apis.loan.entity.EmiFrequency;
import com.banking.banking_app_apis.loan.entity.InterestType;
import com.banking.banking_app_apis.loan.entity.LoanStatus;
import com.banking.banking_app_apis.loan.entity.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long id;
    private String name;
    private String loanSource;
    private String loanAccountNumber;
    private LoanType loanType;
    private BigDecimal principal;
    private BigDecimal annualInterest;
    private Integer tenure;
    private EmiFrequency emiFrequency;
    private InterestType interestType;
    private BigDecimal emiAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalPayableAmount;
    private BigDecimal outstandingAmount;
    private Integer emiPaid;
    private Integer emiPending;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextEmiDate;
    private LocalDate closureDate;
    private LoanStatus status;
    private Boolean autoDebitEnabled;
    private BigDecimal processingFee;
    private BigDecimal insuranceAmount;
    private BigDecimal preClosureAmount;
    private String linkedAccountNumber;

}