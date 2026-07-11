package com.banking.banking_app_apis.loan.util;

import com.banking.banking_app_apis.loan.entity.Loan;
import com.banking.banking_app_apis.loan.entity.LoanSchedule;
import com.banking.banking_app_apis.loan.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LoanScheduleGenerator {

    private final LoanCalculator loanCalculator;

    public List<LoanSchedule> generate(
            Loan loan,
            LoanCalculationResult calculation
    ) {
        BigDecimal interestRate = calculation.getInterestRate();
        int totalEmis = calculation.getTotalEmis();

        BigDecimal outstandingBalance = loan.getPrincipal();
        BigDecimal emiAmount = calculation.getEmiAmount();

        List<LoanSchedule> schedules = new ArrayList<>();

        for (int emiMonth = 1; emiMonth <= totalEmis; emiMonth++) {

            // Interest for current EMI
            BigDecimal interestComponent = outstandingBalance
                    .multiply(interestRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // Principal part of EMI
            BigDecimal principalComponent = emiAmount
                    .subtract(interestComponent)
                    .setScale(2, RoundingMode.HALF_UP);

            // Last EMI adjustment
            if (emiMonth == totalEmis) {
                principalComponent = outstandingBalance;
            }

            BigDecimal lastEmi =
                    principalComponent.add(interestComponent);

            // Remaining balance
            outstandingBalance = outstandingBalance
                    .subtract(principalComponent)
                    .max(BigDecimal.ZERO);

            LocalDate dueDate = loanCalculator.calculateDueDate(loan.getStartDate(), emiMonth, loan.getEmiFrequency());

            LoanSchedule schedule = LoanSchedule.builder()
                    .loan(loan)
                    .emiNumber(emiMonth)
                    .dueDate(dueDate)
                    .month(dueDate.getMonthValue())
                    .year(dueDate.getYear())
                    .principalComponent(principalComponent)
                    .interestComponent(interestComponent)
                    .balanceAfterPayment(outstandingBalance)
                    .penaltyAmount(BigDecimal.ZERO)
                    .status(PaymentStatus.PENDING)
                    .emiAmount(emiAmount)
                    .build();

            schedules.add(schedule);
        }

        return schedules;
    }
}
