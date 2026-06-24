package com.banking.banking_app_apis.user.service;

import com.banking.banking_app_apis.account.constants.AccountConstants;
import com.banking.banking_app_apis.account.dto.AccountSummaryResponse;
import com.banking.banking_app_apis.account.dto.CreditDebitRequest;
import com.banking.banking_app_apis.account.dto.EnquiryRequest;
import com.banking.banking_app_apis.account.dto.TransferRequest;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.entity.AccountStatus;
import com.banking.banking_app_apis.account.entity.AccountType;
import com.banking.banking_app_apis.account.entity.CurrencyType;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.account.service.AccountNumberGenerator;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.common.exception.DuplicateAccountException;
import com.banking.banking_app_apis.common.exception.InsufficientBalanceException;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.notification.dto.EmailDetails;
import com.banking.banking_app_apis.notification.service.EmailService;
import com.banking.banking_app_apis.security.JwtTokenProvider;
import com.banking.banking_app_apis.transaction.dto.TransactionRequest;
import com.banking.banking_app_apis.transaction.entity.Transaction;
import com.banking.banking_app_apis.transaction.service.TransactionService;
import com.banking.banking_app_apis.user.dto.LoginRequest;
import com.banking.banking_app_apis.user.dto.UpdateUserRequest;
import com.banking.banking_app_apis.user.entity.Role;
import com.banking.banking_app_apis.user.entity.User;
import com.banking.banking_app_apis.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    TransactionService transactionService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    AccountNumberGenerator accountNumberGenerator;

    @Autowired
    AccountRepository accountRepository;

    /**
     * Creating an account - saving a new user into the db
     * Check if user already has an account
     */
    @Override
    public BankResponse createAccount(UpdateUserRequest updateUserRequest) {

        if(userRepository.existsByEmail(updateUserRequest.getEmail())) {
            throw new DuplicateAccountException("Account with this email already exists: "
                    + updateUserRequest.getEmail());
        }

        User newUser = User.builder()
                .firstName(updateUserRequest.getFirstName())
                .lastName(updateUserRequest.getLastName())
                .otherName(updateUserRequest.getOtherName())
                .gender(updateUserRequest.getGender())
                .address(updateUserRequest.getAddress())
                .stateOfOrigin(updateUserRequest.getStateOfOrigin())
                .email(updateUserRequest.getEmail())
                .password(passwordEncoder.encode(updateUserRequest.getPassword()))
                .phoneNumber(updateUserRequest.getPhoneNumber())
                .alternativePhoneNumber(updateUserRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(newUser);

        Account account = Account.builder()
                .accountType(AccountType.SAVINGS)
                .balance(BigDecimal.ZERO)
                .currency(CurrencyType.INR)
                .user(savedUser)
                .accountName("Savings Account")
                .accountNumber(accountNumberGenerator.generateAccountNumber())
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody(
                        "Congratulations! Your account has been successfully created.\n\n" +
                                "Your Account Details:\n" +
                                "Account Name: " + account.getAccountName() + "\n" +
                                "Account Number: " + account.getAccountNumber()
                )
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountConstants.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountConstants.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountSummaryResponse(AccountSummaryResponse.builder()
                        .accountBalance(account.getBalance())
                        .accountNumber(account.getAccountNumber())
                        .accountName(account.getAccountName())
                        .build())
                .build();
    }


    public BankResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        // Send Login Email
        EmailDetails loginAlert = EmailDetails.builder()
                .subject("You're logged in!")
                .recipient(loginRequest.getEmail())
                .messageBody("You logged into your account. If you did not initiate this request, please contact your" +
                        " bank!")
                .build();

        emailService.sendEmailAlert(loginAlert);

        return BankResponse.builder()
                .responseCode("Login Success")
                .responseMessage(jwtTokenProvider.generateToken(authentication))
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
    public BankResponse balanceEnquiry(EnquiryRequest enquiryRequest) {
        // Check if the provided account number exists in the DB
        boolean isAccountExists = accountRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + enquiryRequest.getAccountNumber());
        }

        Account userAccount = accountRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return BankResponse.builder()
                .responseCode(AccountConstants.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountConstants.ACCOUNT_FOUND_MESSAGE)
                .accountSummaryResponse(AccountSummaryResponse.builder()
                        .accountBalance(userAccount.getBalance())
                        .accountNumber(userAccount.getAccountNumber())
                        .accountName(userAccount.getAccountName())
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        // Check if the provided account number exists in the DB
        boolean isAccountExists = accountRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + enquiryRequest.getAccountNumber());
        }

        Account account = accountRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return buildFullName(account.getUser());
    }

    @Override
    public BankResponse creditAmount(CreditDebitRequest creditDebitRequest) {

        String reference = UUID.randomUUID().toString();

        // Check if the provided account number exists in the DB
        boolean isAccountExists = accountRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + creditDebitRequest.getAccountNumber());
        }

        Account accountToCredit = accountRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

        BigDecimal currentBalance = accountToCredit.getBalance();
        accountToCredit.setBalance(currentBalance.add(creditDebitRequest.getAmount()));
        accountRepository.save(accountToCredit);

        // Save Transaction
        transactionService.saveTransaction(buildTransactionRequest(accountToCredit, "CREDIT",
                creditDebitRequest.getAmount(), reference, creditDebitRequest.getSource()));

        return BankResponse.builder()
                .responseCode(AccountConstants.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountConstants.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountSummaryResponse(AccountSummaryResponse.builder()
                        .accountBalance(accountToCredit.getBalance())
                        .accountNumber(accountToCredit.getAccountNumber())
                        .accountName(accountToCredit.getAccountName())
                        .build())
                .counterpartySource(creditDebitRequest.getSource())
                .build();
    }

    @Override
    public BankResponse debitAmount(CreditDebitRequest creditDebitRequest) {

        String reference = UUID.randomUUID().toString();

        // Check if the provided account number exists in the DB
        boolean isAccountExists = accountRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + creditDebitRequest.getAccountNumber());
        }

        // Check if amount you intent to withdraw is not more than the current account balance
        Account accountToDebit = accountRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());

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
                        reference, creditDebitRequest.getDestination())
        );

        return BankResponse.builder()
                .responseCode(AccountConstants.ACCOUNT_DEBITED_SUCCESS_CODE)
                .responseMessage(AccountConstants.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                .transactionId(savedTransaction.getTransactionId())
                .accountSummaryResponse(AccountSummaryResponse.builder()
                        .accountBalance(accountToDebit.getBalance())
                        .accountNumber(creditDebitRequest.getAccountNumber())
                        .accountName(accountToDebit.getAccountName())
                        .build())
                .counterpartySource(creditDebitRequest.getDestination())
                .build();
    }

    @Override
    public BankResponse transfer(TransferRequest transferRequest) {

        String reference = UUID.randomUUID().toString();

        // Get the account to debit (Check source account exists)
        boolean isSourceAccountExists = accountRepository.existsByAccountNumber(transferRequest.getSourceAccountNumber());
        if (!isSourceAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: " + transferRequest.getSourceAccountNumber());
        }

        // Get the account to credit (Check destination account exists)
        boolean isDestinationAccountExists = accountRepository.existsByAccountNumber(transferRequest.getDestinationAccountNumber());
        if(!isDestinationAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: " + transferRequest.getDestinationAccountNumber());
        }

        // Check if the amount debited is not more than the current balance
        Account sourceAccount = accountRepository.findByAccountNumber(transferRequest.getSourceAccountNumber());

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
        Account destinationAccount = accountRepository.findByAccountNumber(transferRequest.getDestinationAccountNumber());

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

        return BankResponse.builder()
                .responseCode(AccountConstants.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountConstants.TRANSFER_SUCCESS_MESSAGE)
                .accountSummaryResponse(null)
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
