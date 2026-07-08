package com.banking.banking_app_apis.loan.service;

import com.banking.banking_app_apis.dto.CreateLoanRequest;
import com.banking.banking_app_apis.loan.dto.LoanResponse;
import com.banking.banking_app_apis.loan.entity.*;
import com.banking.banking_app_apis.entity.*;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.exception.ValidationException;
import com.banking.banking_app_apis.loan.repository.LoanRepaymentRepository;
import com.banking.banking_app_apis.loan.repository.LoanRepository;
import com.banking.banking_app_apis.loan.repository.LoanScheduleRepository;
import com.banking.banking_app_apis.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanScheduleRepository loanScheduleRepository;

    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;

    @Autowired
    private UserRepository userRepository;


    @Transactional
    @Override
    public LoanResponse createLoan(CreateLoanRequest request, User currentUser) {

        User user = userRepository.findByAccountNumber(request.getLinkedAccountNumber());
        if(user == null) {
            throw new ResourceNotFoundException("No user found with the account number: " + request.getLinkedAccountNumber());
        }

        if(!Objects.equals(user.getId(), currentUser.getId())) {
            throw new ValidationException("You cannot create loan entry for another user!");
        }

        BigDecimal annualInterestRate = request.getAnnualInterest();
        BigDecimal loanAmount = request.getPrincipal();
        int tenureYears = request.getTenure();

        BigDecimal interestRate = BigDecimal.ZERO;
        int totalEmis = 0;

        // Loan End Date
        LocalDate startDate = request.getStartDate();
        LocalDate nextEmiDate = startDate;

        if (request.getEmiFrequency() == EmiFrequency.MONTHLY) {
            interestRate = annualInterestRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            totalEmis = tenureYears * 12;
        } else if (request.getEmiFrequency() == EmiFrequency.QUARTERLY) {
            interestRate = annualInterestRate.divide(BigDecimal.valueOf(400), 10, RoundingMode.HALF_UP);
            totalEmis = tenureYears * 4;
        } else if (request.getEmiFrequency() == EmiFrequency.YEARLY) {
            interestRate = annualInterestRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            totalEmis = tenureYears;
        }

        LocalDate endDate = calculateDueDate(startDate, totalEmis, request.getEmiFrequency());

        // Calculate EMI Amount based on Interest Type
        BigDecimal emiAmount;
        BigDecimal totalPayableAmount;
        BigDecimal totalInterestAmount;

        switch (request.getInterestType()) {

            case REDUCING -> {

                emiAmount = calculateReducingEmi(
                        interestRate,
                        totalEmis,
                        loanAmount
                );

                totalPayableAmount = emiAmount.multiply(
                        BigDecimal.valueOf(totalEmis));

                totalInterestAmount = totalPayableAmount.subtract(loanAmount);
            }

            case FIXED -> {

                BigDecimal totalInterest = loanAmount
                        .multiply(annualInterestRate)
                        .multiply(BigDecimal.valueOf(tenureYears))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                totalPayableAmount = loanAmount.add(totalInterest);

                totalInterestAmount = totalInterest;

                emiAmount = totalPayableAmount.divide(
                        BigDecimal.valueOf(totalEmis),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            case FLOATING -> {

                // For now, treat floating same as reducing.
                // Later interest rate changes can regenerate schedules.

                emiAmount = calculateReducingEmi(
                        interestRate,
                        totalEmis,
                        loanAmount
                );

                totalPayableAmount = emiAmount.multiply(
                        BigDecimal.valueOf(totalEmis));

                totalInterestAmount = totalPayableAmount.subtract(loanAmount);
            }

            default -> throw new ValidationException("Invalid interest type");
        }

        // Loan Builder
        Loan loan = Loan.builder()
                .name(request.getName())
                .loanSource(request.getLoanSource())
                .loanAccountNumber(request.getLoanAccountNumber())
                .loanType(request.getLoanType())
                .principal(loanAmount)
                .annualInterest(annualInterestRate)
                .tenure(tenureYears)
                .emiFrequency(request.getEmiFrequency())
                .interestType(request.getInterestType())
                .startDate(startDate)
                .insuranceAmount(
                        request.getInsuranceAmount() == null
                                ? BigDecimal.ZERO
                                : request.getInsuranceAmount()
                )
                .processingFee(
                        request.getProcessingFee() == null
                                ? BigDecimal.ZERO
                                : request.getProcessingFee()
                )
                .autoDebitEnabled(request.getAutoDebitEnabled())
                .linkedAccount(user.getAccountNumber())
                .emiAmount(emiAmount)
                .totalPayableAmount(totalPayableAmount)
                .totalInterest(totalInterestAmount)
                .outstandingAmount(loanAmount)
                .endDate(endDate)
                .nextEmiDate(nextEmiDate)
                .emiPaid(0)
                .emiPending(totalEmis)
                .status(LoanStatus.ACTIVE)
                .user(currentUser)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        List<LoanSchedule> schedules = generateSchedules(savedLoan, interestRate, totalEmis);

        loanScheduleRepository.saveAll(schedules);

        return mapToLoanResponse(savedLoan);
    }


    private BigDecimal calculateReducingEmi(BigDecimal interestRate, int totalEmis, BigDecimal loanAmount) {

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

    private LocalDate calculateDueDate(LocalDate startDate, int emiNumber, EmiFrequency frequency) {

        return switch (frequency) {
            case MONTHLY ->
                    startDate.plusMonths(emiNumber - 1L);
            case QUARTERLY ->
                    startDate.plusMonths((emiNumber - 1L) * 3L);
            case YEARLY ->
                    startDate.plusYears(emiNumber - 1L);
        };
    }


    private List<LoanSchedule> generateSchedules(Loan loan, BigDecimal interestRate, int totalEmis) {
        List<LoanSchedule> schedules = new ArrayList<>();

        BigDecimal outstandingBalance = loan.getPrincipal();
        BigDecimal emiAmount = loan.getEmiAmount();

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

            LocalDate dueDate = calculateDueDate(loan.getStartDate(), emiMonth, loan.getEmiFrequency());

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


    private LoanResponse mapToLoanResponse(Loan loan) {
        return LoanResponse.builder()
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
                .linkedAccountNumber(loan.getLinkedAccount())
                .build();
    }

}
