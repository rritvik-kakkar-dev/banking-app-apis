package com.banking.banking_app_apis.loan.service;

import com.banking.banking_app_apis.account.dto.CreditDebitRequest;
import com.banking.banking_app_apis.account.dto.TransactionResponse;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.account.service.AccountService;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.loan.dto.*;
import com.banking.banking_app_apis.loan.entity.*;
import com.banking.banking_app_apis.loan.mapper.LoanMapper;
import com.banking.banking_app_apis.loan.repository.LoanRepaymentRepository;
import com.banking.banking_app_apis.loan.repository.LoanRepository;
import com.banking.banking_app_apis.loan.repository.LoanScheduleRepository;
import com.banking.banking_app_apis.loan.util.LoanCalculationResult;
import com.banking.banking_app_apis.loan.util.LoanCalculator;
import com.banking.banking_app_apis.loan.util.LoanScheduleGenerator;
import com.banking.banking_app_apis.loan.util.LoanValidator;
import com.banking.banking_app_apis.transaction.entity.Transaction;
import com.banking.banking_app_apis.transaction.service.TransactionService;
import com.banking.banking_app_apis.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.banking.banking_app_apis.account.service.AccountServiceImpl.buildTransactionRequest;

@RequiredArgsConstructor
@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final AccountRepository accountRepository;

    private final LoanCalculator loanCalculator;
    private final LoanScheduleGenerator loanScheduleGenerator;
    private final LoanMapper loanMapper;

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final LoanValidator loanValidator;


    @Override
    public LoanDetailResponse createLoan(CreateLoanRequest request, User currentUser) {

        loanValidator.validate(request);

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("This account does not belong to you.");
        }

        LoanCalculationResult calculation =
                loanCalculator.calculate(request);

        // Loan Builder
        Loan loan = Loan.builder()
                .name(request.getName())
                .loanSource(request.getLoanSource())
                .loanAccountNumber(request.getLoanAccountNumber())
                .loanType(request.getLoanType())
                .startDate(request.getStartDate())
                .nextEmiDate(calculation.getNextEmiDate())
                .principal(request.getPrincipal())
                .annualInterest(request.getAnnualInterest())
                .tenure(request.getTenure())
                .emiFrequency(request.getEmiFrequency())
                .interestType(request.getInterestType())
                .insuranceAmount(defaultZero(request.getInsuranceAmount()))
                .processingFee(defaultZero(request.getProcessingFee()))
                .autoDebitEnabled(request.getAutoDebitEnabled())
                .account(account)
                .user(currentUser)
                .emiPaid(0)
                .status(LoanStatus.ACTIVE)
                .emiAmount(calculation.getEmiAmount())
                .totalInterest(calculation.getTotalInterest())
                .totalPayableAmount(calculation.getTotalPayable())
                .outstandingAmount(request.getPrincipal())
                .endDate(calculation.getEndDate())
                .emiPending(calculation.getTotalEmis())
                .build();

        Loan savedLoan = loanRepository.save(loan);

        List<LoanSchedule> schedules =
                loanScheduleGenerator.generate(
                        savedLoan,
                        calculation
                );

        loanScheduleRepository.saveAll(schedules);

        return loanMapper.toResponse(savedLoan);
    }

    @Override
    public LoanDetailResponse getLoan(Long id, User currentUser) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Not able to find Loan with id " + id));

        if (!loan.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("You don't have access to this loan.");
        }

        return LoanDetailResponse.builder()
                .name(loan.getName())
                .loanSource(loan.getLoanSource())
                .loanAccountNumber(loan.getLoanAccountNumber())
                .loanType(loan.getLoanType())
                .startDate(loan.getStartDate())
                .nextEmiDate(loan.getNextEmiDate())
                .principal(loan.getPrincipal())
                .annualInterest(loan.getAnnualInterest())
                .tenure(loan.getTenure())
                .emiFrequency(loan.getEmiFrequency())
                .interestType(loan.getInterestType())
                .insuranceAmount(defaultZero(loan.getInsuranceAmount()))
                .processingFee(defaultZero(loan.getProcessingFee()))
                .autoDebitEnabled(loan.getAutoDebitEnabled())
                .accountNumber(loan.getAccount().getAccountNumber())
                .accountName(loan.getAccount().getAccountName())
                .emiPaid(loan.getEmiPaid())
                .status(loan.getStatus())
                .emiAmount(loan.getEmiAmount())
                .totalInterest(loan.getTotalInterest())
                .totalPayableAmount(loan.getTotalPayableAmount())
                .outstandingAmount(loan.getOutstandingAmount())
                .endDate(loan.getEndDate())
                .emiPending(loan.getEmiPending())
                .build();
    }

    @Override
    public LoanScheduleDetailsResponse getLoanSchedule(Long id, User currentUser) {

        Loan loan = loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Loan found with id " + id));

        if (!loan.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("You don't have access to this loan.");
        }

        List<LoanScheduleResponse> schedules =
                loanScheduleRepository.findByLoanId(loan.getId())
                        .stream()
                        .map(loanMapper::toScheduleResponse)
                        .toList();

        return LoanScheduleDetailsResponse.builder()
                .loanId(loan.getId())
                .loanName(loan.getName())
                .loanAccountNumber(loan.getLoanAccountNumber())
                .principal(loan.getPrincipal())
                .outstandingAmount(loan.getOutstandingAmount())
                .emiAmount(loan.getEmiAmount())
                .emiPaid(loan.getEmiPaid())
                .emiPending(loan.getEmiPending())
                .schedules(schedules)
                .build();
    }

    @Override
    public Page<LoanSummaryResponse> getLoans(User currentUser, int page, int limit, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, limit, sort);

        return loanRepository
                .findByUser(currentUser, pageable)
                .map(loanMapper::toSummaryResponse);
    }

    @Override
    public PayEmiResponse payEmi(Long id, PayEmiRequest request, User currentUser) {

        if (request.getAmount() == null
                || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Invalid EMI amount.");
        }

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + request.getAccountId()));

        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("This account does not belong to you.");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new ValidationException("Insufficient account balance.");
        }

        Loan loan = loanRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Loan found with ID: " + id));

        if (!loan.getAccount().getId().equals(account.getId())) {
            throw new ValidationException(
                    "This loan is linked to a different account."
            );
        }

        boolean isOwner = loan.getUser().getId().equals(currentUser.getId());

        if (!isOwner) {
            throw new ValidationException("You are not the owner of this loan.");
        }

        if(loan.getStatus() == LoanStatus.CLOSED) {
            throw new ValidationException("Loan is already closed!!");
        }

        LoanSchedule loanSchedule = loanScheduleRepository.findByLoanAndMonthAndYear(loan, request.getMonth(), request.getYear()).orElseThrow(() -> new ResourceNotFoundException("No schedule found for loan ID: " + id));

        if (loanSchedule.getStatus() == PaymentStatus.PAID) {
            throw new ValidationException("EMI for the selected month is already paid.");
        }

        // Debit Account
        CreditDebitRequest debitRequest = CreditDebitRequest.builder()
                .accountNumber(account.getAccountNumber())
                .amount(request.getAmount())
                .destinationDescription("Loan payment")
                .build();

        TransactionResponse debitAmount = accountService.debit(debitRequest);

        // Save Transaction
        String reference = UUID.randomUUID().toString();

        Transaction transaction = transactionService.saveTransaction(
                buildTransactionRequest(account, "DEBIT", debitRequest.getAmount(),
                        reference, debitRequest.getDestinationDescription())
        );

        // Repayment
        LoanRepayment loanRepayment = LoanRepayment.builder()
                .loan(loan)
                .emiNumber(loanSchedule.getEmiNumber())
                .amountPaid(request.getAmount())
                .paymentDate(LocalDate.now())
                .principalComponent(loanSchedule.getPrincipalComponent())
                .outstandingBalance(loanSchedule.getBalanceAfterPayment())
                .interestComponent(loanSchedule.getInterestComponent())
                .transaction(transaction)
                .status(PaymentStatus.PAID)
                .remarks("EMI paid successfully")
                .build();

        LoanRepayment loanRepaymentSaved = loanRepaymentRepository.save(loanRepayment);

        // Update Loan Schedule
        loanSchedule.setPaidDate(LocalDate.now());
        loanSchedule.setStatus(PaymentStatus.PAID);
        loanScheduleRepository.save(loanSchedule);

        // Update Loan
        loan.setOutstandingAmount(loanSchedule.getBalanceAfterPayment());
        loan.setEmiPaid(loan.getEmiPaid() + 1);
        loan.setEmiPending(loan.getEmiPending() - 1);

        if (loan.getEmiPending() == 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosureDate(LocalDate.now());
        }

        loanRepository.save(loan);

        loanScheduleRepository.findFirstByLoanAndStatusOrderByDueDateAsc(loan, PaymentStatus.PENDING)
                .ifPresent(next -> {
                    loan.setNextEmiDate(next.getDueDate());
                    loanRepository.save(loan);
                });

        return PayEmiResponse.builder()
                .loanId(loan.getId())
                .emiNumber(loanSchedule.getEmiNumber())
                .month(request.getMonth())
                .year(request.getYear())
                .amountPaid(request.getAmount())
                .principalComponent(loanRepayment.getPrincipalComponent())
                .interestComponent(loanRepayment.getInterestComponent())
                .outstandingAmount(loanRepayment.getOutstandingBalance())
                .emiPaid(loan.getEmiPaid())
                .emiPending(loan.getEmiPending())
                .paymentDate(loanSchedule.getPaidDate())
                .nextEmiDate(loanSchedule.getDueDate())
                .loanStatus(loan.getStatus())
                .transactionReference(reference)
                .message("EMI Paid Successfully")
                .build();

    }


    private Account getCurrentUserAccount(User currentUser) {

        return accountRepository.findByUser(currentUser)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No account found for the current user."));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

}
