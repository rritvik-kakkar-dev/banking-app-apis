package com.banking.banking_app_apis.loan.mapper;

import com.banking.banking_app_apis.loan.dto.LoanDetailResponse;
import com.banking.banking_app_apis.loan.dto.LoanScheduleResponse;
import com.banking.banking_app_apis.loan.dto.LoanSummaryResponse;
import com.banking.banking_app_apis.loan.entity.Loan;
import com.banking.banking_app_apis.loan.entity.LoanSchedule;
import org.springframework.stereotype.Component;

@Component
public class LoanMapper {

    public LoanDetailResponse toResponse(Loan loan) {
        return LoanDetailResponse.builder()
                .id(loan.getId())
                .name(loan.getName())
                .loanSource(loan.getLoanSource())
                .loanAccountNumber(loan.getLoanAccountNumber())
                .loanType(loan.getLoanType())
                .principal(loan.getPrincipal())
                .annualInterest(loan.getAnnualInterest())
                .tenure(loan.getTenure())
                .emiFrequency(loan.getEmiFrequency())
                .interestType(loan.getInterestType())
                .emiAmount(loan.getEmiAmount())
                .totalInterest(loan.getTotalInterest())
                .totalPayableAmount(loan.getTotalPayableAmount())
                .outstandingAmount(loan.getOutstandingAmount())
                .emiPaid(loan.getEmiPaid())
                .emiPending(loan.getEmiPending())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .nextEmiDate(loan.getNextEmiDate())
                .closureDate(loan.getClosureDate())
                .status(loan.getStatus())
                .autoDebitEnabled(loan.getAutoDebitEnabled())
                .processingFee(loan.getProcessingFee())
                .insuranceAmount(loan.getInsuranceAmount())
                .preClosureAmount(loan.getPreClosureAmount())
                .accountName(loan.getAccount().getAccountName())
                .accountNumber(loan.getAccount().getAccountNumber())
                .build();
    }

    public LoanSummaryResponse toSummaryResponse(Loan loan) {
        return LoanSummaryResponse.builder()
                .id(loan.getId())
                .name(loan.getName())
                .loanType(loan.getLoanType())
                .principal(loan.getPrincipal())
                .outstandingAmount(loan.getOutstandingAmount())
                .emiAmount(loan.getEmiAmount())
                .status(loan.getStatus())
                .build();
    }

    public LoanScheduleResponse toScheduleResponse(LoanSchedule schedule) {

        return LoanScheduleResponse.builder()
                .emiNumber(schedule.getEmiNumber())
                .dueDate(schedule.getDueDate())
                .emiAmount(schedule.getEmiAmount())
                .principalComponent(schedule.getPrincipalComponent())
                .interestComponent(schedule.getInterestComponent())
                .balanceAfterPayment(schedule.getBalanceAfterPayment())
                .penaltyAmount(schedule.getPenaltyAmount())
                .status(schedule.getStatus())
                .build();
    }
}
