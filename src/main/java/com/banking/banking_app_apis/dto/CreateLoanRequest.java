package com.banking.banking_app_apis.dto;

import com.banking.banking_app_apis.entity.EmiFrequency;
import com.banking.banking_app_apis.entity.InterestType;
import com.banking.banking_app_apis.entity.LoanType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLoanRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String loanSource;

    private String loanAccountNumber;

    @NotNull
    private LoanType loanType;

    @NotNull
    private BigDecimal principal;

    @NotNull
    private BigDecimal annualInterest;

    @NotNull
    private Integer tenure;

    @NotNull
    private LocalDate startDate;

    private EmiFrequency emiFrequency = EmiFrequency.MONTHLY;

    private InterestType interestType = InterestType.REDUCING;

    private BigDecimal processingFee = BigDecimal.ZERO;

    private BigDecimal insuranceAmount = BigDecimal.ZERO;

    private Boolean autoDebitEnabled = false;

    @NotNull
    private String linkedAccountNumber;
}
