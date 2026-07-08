package com.banking.banking_app_apis.account.service;

import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.common.exception.InsufficientBalanceException;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.transaction.dto.TransactionRequest;
import com.banking.banking_app_apis.transaction.entity.Transaction;
import com.banking.banking_app_apis.transaction.service.TransactionService;
import com.banking.banking_app_apis.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {


    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;


    
    @Override
    public Account createDefaultAccount(User user) {

        String accountNumber;

        do {
            accountNumber = accountNumberGenerator.generateAccountNumber();
        } while(accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .currency(CurrencyType.INR)
                .user(user)
                .accountName("Savings Account")
                .accountNumber(accountNumber)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        return account;
    }

    /**
     * Balance Enquiry
     * Name Enquiry
     * Credit
     * Debit
     * Transfer
     */
    @Override
    public BalanceResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
        Account account =
                accountRepository.findByAccountNumber(enquiryRequest.getAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + enquiryRequest.getAccountNumber()));

        return BalanceResponse.builder()
                .currency(account.getCurrency().name())
                .balance(account.getBalance())
                .accountNumber(account.getAccountNumber())
                .build();
    }

    @Override
    public AccountNameResponse nameEnquiry(EnquiryRequest enquiryRequest) {

        Account account = accountRepository.findByAccountNumber(enquiryRequest.getAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + enquiryRequest.getAccountNumber()));

        return AccountNameResponse.builder()
                .accountHolderName(buildFullName(account.getUser()))
                .accountNumber(account.getAccountNumber())
                .build();
    }

    @Override
    public TransactionResponse credit(CreditDebitRequest creditDebitRequest) {

        String reference = UUID.randomUUID().toString();

        Account accountToCredit = accountRepository.findByAccountNumber(creditDebitRequest.getAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + creditDebitRequest.getAccountNumber()));

        BigDecimal currentBalance = accountToCredit.getBalance();
        accountToCredit.setBalance(currentBalance.add(creditDebitRequest.getAmount()));
        accountRepository.save(accountToCredit);

        // Save Transaction
        Transaction transaction = transactionService.saveTransaction(buildTransactionRequest(accountToCredit, "CREDIT",
                creditDebitRequest.getAmount(), reference, creditDebitRequest.getSourceDescription()));

        return TransactionResponse.builder()
                .amount(creditDebitRequest.getAmount())
                .destinationAccountNumber(accountToCredit.getAccountNumber())
                .destinationAccountName(accountToCredit.getAccountName())
                .transactionReference(reference)
                .transactionId(transaction.getTransactionId())
                .createdAt(LocalDateTime.now())
                .transactionType("CREDIT")
                .status("SUCCESS")
                .build();
    }

    @Override
    public TransactionResponse debit(CreditDebitRequest creditDebitRequest) {

        String reference = UUID.randomUUID().toString();

        // Check if the provided account number exists in the DB
        boolean isAccountExists = accountRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + creditDebitRequest.getAccountNumber());
        }

        // Check if amount you intent to withdraw is not more than the current account balance
        Account accountToDebit = accountRepository.findByAccountNumber(creditDebitRequest.getAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Account not found with account number: " + creditDebitRequest.getAccountNumber()));

        BigDecimal currentBalance = accountToDebit.getBalance();

        if (creditDebitRequest.getAmount().compareTo(accountToDebit.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient Balance: "
                    + creditDebitRequest.getAmount());
        }

        accountToDebit.setBalance(currentBalance.subtract(creditDebitRequest.getAmount()));
        accountRepository.save(accountToDebit);

        // Save Transaction
        Transaction savedTransaction = transactionService.saveTransaction(
                buildTransactionRequest(accountToDebit, "DEBIT", creditDebitRequest.getAmount(),
                        reference, creditDebitRequest.getDestinationDescription())
        );

        return TransactionResponse.builder()
                .amount(creditDebitRequest.getAmount())
                .sourceAccountNumber(accountToDebit.getAccountNumber())
                .sourceAccountName(accountToDebit.getAccountName())
                .transactionReference(reference)
                .transactionId(savedTransaction.getTransactionId())
                .createdAt(LocalDateTime.now())
                .transactionType("DEBIT")
                .status("SUCCESS")
                .build();
    }

    @Override
    public TransferResponse transfer(TransferRequest transferRequest) {

        String reference = UUID.randomUUID().toString();

        // Get the account to debit (Check source account exists)
        boolean isSourceAccountExists = accountRepository.existsByAccountNumber(transferRequest.getSourceAccountNumber());
        if (!isSourceAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: " + transferRequest.getSourceAccountNumber());
        }

        // Check if the amount debited is not more than the current balance
        Account sourceAccount = accountRepository.findByAccountNumber(transferRequest.getSourceAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Source Account not found with account number: " + transferRequest.getSourceAccountNumber()));

        if(transferRequest.getAmount().compareTo(sourceAccount.getBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient Balance: "
                    + transferRequest.getAmount());
        }

        // Debit amount
        BigDecimal currentSourceAccountBalance = sourceAccount.getBalance();
        sourceAccount.setBalance(currentSourceAccountBalance.subtract(transferRequest.getAmount()));
        accountRepository.save(sourceAccount);

        // Save Transaction
        transactionService.saveTransaction(buildTransactionRequest(sourceAccount, "DEBIT",
                transferRequest.getAmount(), reference, transferRequest.getDestinationAccountNumber()));

        // Send Debit Amount Email Alert
        //        EmailDetails debitAlert = EmailDetails.builder()
        //                .subject("DEBIT ALERT")
        //                .recipient(sourceAccount.getEmail())
        //                .messageBody("The sum of " + transferRequest.getAmount() + " has been deducted from you account! Your Current Balance is " + sourceAccount.getAccountBalance())
        //                .build();
        //
        //        emailService.sendEmailAlert(debitAlert);

        // Get the account to credit
        Account destinationAccount = accountRepository.findByAccountNumber(transferRequest.getDestinationAccountNumber()).orElseThrow(() -> new ResourceNotFoundException("Destination Account not found with account number: " + transferRequest.getDestinationAccountNumber()));

        BigDecimal currentDestinationAccountBalance = destinationAccount.getBalance();

        // Credit the amount
        destinationAccount.setBalance(currentDestinationAccountBalance.add(transferRequest.getAmount()));
        accountRepository.save(destinationAccount);

        // Save Transaction

        transactionService.saveTransaction(buildTransactionRequest(destinationAccount, "CREDIT",
                transferRequest.getAmount(), reference, transferRequest.getSourceAccountNumber()));

        // Send Credit Amount Email Alert
        //        EmailDetails creditAlert = EmailDetails.builder()
        //                .subject("CREDIT ALERT")
        //                .recipient(destinationAccount.getEmail())
        //                .messageBody("The sum of " + transferRequest.getAmount() + " has been credited to you account from " + sourceAccountUserFullName + ". Your Current Balance is " + destinationAccount.getAccountBalance())
        //                .build();
        //
        //        emailService.sendEmailAlert(creditAlert);

        return TransferResponse.builder()
                .amount(transferRequest.getAmount())
                .sourceAccount(sourceAccount.getAccountNumber())
                .destinationAccount(destinationAccount.getAccountNumber())
                .transactionReference(reference)
                .transferredAt(LocalDateTime.now())
                .transactionStatus("SUCCESS")
                .build();

    }

    private String buildFullName(User user) {
        return Stream.of(user.getFirstName(), user.getLastName(), user.getOtherName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));
    }

    private TransactionRequest buildTransactionRequest(Account account, String type, BigDecimal amount, String reference
            , String source) {
        return TransactionRequest.builder()
                .transactionReference(reference)
                .account(account)
                .transactionType(type)
                .amount(amount)
                .counterpartyAccountNumber(source)
                .build();
    }
}
