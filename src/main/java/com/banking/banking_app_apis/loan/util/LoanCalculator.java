package com.banking.banking_app_apis.loan.util;

import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.loan.dto.CreateLoanRequest;
import com.banking.banking_app_apis.loan.entity.EmiFrequency;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
public class LoanCalculator {


    public LoanCalculationResult calculate(CreateLoanRequest request) {

        BigDecimal annualInterest = request.getAnnualInterest();
        BigDecimal principal = request.getPrincipal();
        int tenureYears = request.getTenure();

        BigDecimal interestRate = calculateInterestRate(
                annualInterest,
                request.getEmiFrequency());

        int totalEmis = calculateTotalEmis(
                tenureYears,
                request.getEmiFrequency());

        LocalDate endDate = calculateDueDate(
                request.getStartDate(),
                totalEmis,
                request.getEmiFrequency());

        BigDecimal emiAmount;
        BigDecimal totalInterest;
        BigDecimal totalPayable;

        switch (request.getInterestType()) {

            case REDUCING, FLOATING -> {

                emiAmount = calculateReducingEmi(
                        interestRate,
                        totalEmis,
                        principal);

                totalPayable = emiAmount.multiply(
                        BigDecimal.valueOf(totalEmis));

                totalInterest = totalPayable.subtract(principal);
            }

            case FIXED -> {

                totalInterest = principal
                        .multiply(annualInterest)
                        .multiply(BigDecimal.valueOf(tenureYears))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalPayable = principal.add(totalInterest);

                emiAmount = totalPayable.divide(
                        BigDecimal.valueOf(totalEmis),
                        2,
                        RoundingMode.HALF_UP);
            }

            default ->
                    throw new ValidationException("Invalid interest type");
        }

        return LoanCalculationResult.builder()
                .interestRate(interestRate)
                .totalEmis(totalEmis)
                .emiAmount(emiAmount)
                .totalInterest(totalInterest)
                .totalPayable(totalPayable)
                .endDate(endDate)
                .build();
    }

    public BigDecimal calculateReducingEmi(BigDecimal interestRate, int totalEmis, BigDecimal loanAmount) {

        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return loanAmount.divide(
                    BigDecimal.valueOf(totalEmis),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        BigDecimal interestFactor = interestRate.add(BigDecimal.ONE)
                .pow(totalEmis);

        BigDecimal totalAmountWithInterest = loanAmount
                .multiply(interestRate)
                .multiply(interestFactor);

        BigDecimal divisor = interestFactor.subtract(BigDecimal.ONE);

        BigDecimal emiAmount = totalAmountWithInterest.divide(
                divisor,
                2,
                RoundingMode.HALF_UP
        );

        return emiAmount;
    }

    public BigDecimal calculateFixedInterest() {
        return BigDecimal.ZERO;
    }

    public BigDecimal calculateInterestRate(
            BigDecimal annualInterest,
            EmiFrequency frequency) {

        return switch (frequency) {

            case MONTHLY ->
                    annualInterest.divide(
                            BigDecimal.valueOf(1200),
                            10,
                            RoundingMode.HALF_UP);

            case QUARTERLY ->
                    annualInterest.divide(
                            BigDecimal.valueOf(400),
                            10,
                            RoundingMode.HALF_UP);

            case YEARLY ->
                    annualInterest.divide(
                            BigDecimal.valueOf(100),
                            10,
                            RoundingMode.HALF_UP);
        };
    }


    public int calculateTotalEmis(
            int tenureYears,
            EmiFrequency frequency) {

        return switch (frequency) {

            case MONTHLY ->
                    tenureYears * 12;

            case QUARTERLY ->
                    tenureYears * 4;

            case YEARLY ->
                    tenureYears;
        };
    }

    public LocalDate calculateDueDate(LocalDate startDate, int emiNumber, EmiFrequency frequency) {
        return switch (frequency) {
            case MONTHLY ->
                    startDate.plusMonths(emiNumber - 1L);
            case QUARTERLY ->
                    startDate.plusMonths((emiNumber - 1L) * 3L);
            case YEARLY ->
                    startDate.plusYears(emiNumber - 1L);
        };
    }
}
