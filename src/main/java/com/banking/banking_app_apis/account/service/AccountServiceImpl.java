package com.banking.banking_app_apis.account.service;

import com.banking.banking_app_apis.account.constants.AccountConstants;
import com.banking.banking_app_apis.account.dto.*;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import com.banking.banking_app_apis.account.mapper.AccountMapper;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.common.exception.InsufficientBalanceException;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.loan.repository.LoanRepository;
import com.banking.banking_app_apis.transaction.dto.TransactionRequest;
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
    private final AccountMapper accountMapper;
    private final LoanRepository loanRepository;
    
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

    @Override
    public CreateAccountResponse createAccount(CreateAccountRequest request, User user) {

        Account account = Account.builder()
                .accountType(request.getAccountType())
                .accountNumber(request.getAccountNumber())
                .balance(request.getOpeningBalance())
                .accountName(request.getAccountName())
                .currency(CurrencyType.INR)
                .user(user)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        return CreateAccountResponse.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(LocalDate.now())
                .build();
    }

    @Override
    public Page<AccountSummaryResponse> getAccounts(User currentUser, int page, int limit, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, limit, sort);

        return accountRepository
                .findByUser(currentUser, pageable)
                .map(accountMapper::toSummaryResponse);
    }

    @Override
    public AccountSummaryResponse getAccount(Long id, User currentUser) {

        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Account found with ID: " + id));

        return AccountSummaryResponse.builder()
                .id(id)
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Override
    public AccountSummaryResponse updateAccount(Long id, UpdateAccountRequest request, User currentUser) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Account found with ID: " + id));

        if(!account.getUser().equals(currentUser)) {
            throw new ValidationException("Only account owner can update the account details");
        }

        account.setAccountName(request.getAccountName());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency());

        accountRepository.save(account);

        return AccountSummaryResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();

    }

    @Override
    public AccountResponse closeAccount(Long id, User currentUser) {
        Account account = accountRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        if(!account.getUser().equals(currentUser)) {
            throw new ValidationException("Only the account owner can close this account.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException( "Only active accounts can be closed." );
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException(
                    "Account cannot be closed until the balance is zero."
            );
        }

        if (loanRepository.existsActiveLoanByAccount(account)) {
            throw new ValidationException(
                    "Account has active loans."
            );
        }


        account.setStatus(AccountStatus.CLOSED);
        accountRepository.save(account);

        return AccountResponse.builder()
                .message("Account Closed Successfully")
                .status(AccountConstants.SUCCESS)
                .build();
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

    public static TransactionRequest buildTransactionRequest(Account account, String type, BigDecimal amount, String reference
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
