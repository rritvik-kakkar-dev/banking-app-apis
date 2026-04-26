package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.config.JwtTokenProvider;
import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.entity.Role;
import com.banking.banking_app_apis.entity.User;
import com.banking.banking_app_apis.exception.DuplicateAccountException;
import com.banking.banking_app_apis.exception.InsufficientBalanceException;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.repository.UserRepository;
import com.banking.banking_app_apis.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class UserServiceImpl implements UserService{

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

    /**
     * Creating an account - saving a new user into the db
     * Check if user already has an account
     */
    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateAccountException("Account with this email already exists: "
                    + userRequest.getEmail());
        }

        User newUser = User.builder()
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .otherName(userRequest.getOtherName())
                .gender(userRequest.getGender())
                .address(userRequest.getAddress())
                .stateOfOrigin(userRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(newUser);

        // Send email alert
        String fullName = savedUser.getFirstName() + " "
                + savedUser.getLastName()
                + (savedUser.getOtherName() != null ? " " + savedUser.getOtherName() : "");

        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(savedUser.getEmail())
                .subject("ACCOUNT CREATION")
                .messageBody(
                        "Congratulations! Your account has been successfully created.\n\n" +
                                "Your Account Details:\n" +
                                "Account Name: " + fullName + "\n" +
                                "Account Number: " + savedUser.getAccountNumber()
                )
                .build();
        emailService.sendEmailAlert(emailDetails);

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREATION_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREATION_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(savedUser.getAccountBalance())
                        .accountNumber(savedUser.getAccountNumber())
                        .accountName(fullName)
                        .build())
                .build();
    }


    public BankResponse login(LoginDto loginDto) {
        Authentication authentication = null;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        // Send Login Email
        EmailDetails loginAlert = EmailDetails.builder()
                .subject("You're logged in!")
                .recipient(loginDto.getEmail())
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
        boolean isAccountExists = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + enquiryRequest.getAccountNumber());
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        String fullName = foundUser.getFirstName() + " "
                + foundUser.getLastName()
                + (foundUser.getOtherName() != null ? " " + foundUser.getOtherName() : "");

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_FOUND_CODE)
                .responseMessage(AccountUtils.ACCOUNT_FOUND_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(foundUser.getAccountBalance())
                        .accountNumber(foundUser.getAccountNumber())
                        .accountName(fullName)
                        .build())
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest enquiryRequest) {
        // Check if the provided account number exists in the DB
        boolean isAccountExists = userRepository.existsByAccountNumber(enquiryRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + enquiryRequest.getAccountNumber());
        }

        User foundUser = userRepository.findByAccountNumber(enquiryRequest.getAccountNumber());

        return foundUser.getFirstName() + " "
                + foundUser.getLastName()
                + (foundUser.getOtherName() != null ? " " + foundUser.getOtherName() : "");
    }

    @Override
    public BankResponse creditAmount(CreditDebitRequest creditDebitRequest) {
        // Check if the provided account number exists in the DB
        boolean isAccountExists = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + creditDebitRequest.getAccountNumber());
        }

        User userToCredit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        BigDecimal currentBalance = userToCredit.getAccountBalance();
        userToCredit.setAccountBalance(currentBalance.add(creditDebitRequest.getAmount()));
        userRepository.save(userToCredit);

        // Save Transaction
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userToCredit.getAccountNumber())
                .transactionType("CREDIT")
                .amount(creditDebitRequest.getAmount())
                .build();

        transactionService.saveTransaction(transactionDto);

        String fullName = userToCredit.getFirstName() + " "
                + userToCredit.getLastName()
                + (userToCredit.getOtherName() != null ? " " + userToCredit.getOtherName() : "");

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_CREDITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(userToCredit.getAccountBalance())
                        .accountNumber(creditDebitRequest.getAccountNumber())
                        .accountName(fullName)
                        .build())
                .build();
    }

    @Override
    public BankResponse debitAmount(CreditDebitRequest creditDebitRequest) {
        // Check if the provided account number exists in the DB
        boolean isAccountExists = userRepository.existsByAccountNumber(creditDebitRequest.getAccountNumber());
        if(!isAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: "
                    + creditDebitRequest.getAccountNumber());
        }

        // Check if amount you intent to withdraw is not more than the current account balance
        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        BigDecimal currentBalance = userToDebit.getAccountBalance();

        BigInteger availableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = creditDebitRequest.getAmount().toBigInteger();

        if(availableBalance.intValue() < debitAmount.intValue()) {
            throw new InsufficientBalanceException("Insufficient Balance: "
                    + creditDebitRequest.getAmount());
        }

        userToDebit.setAccountBalance(currentBalance.subtract(creditDebitRequest.getAmount()));
        userRepository.save(userToDebit);

        // Save Transaction
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber(userToDebit.getAccountNumber())
                .transactionType("DEBIT")
                .amount(creditDebitRequest.getAmount())
                .build();

        transactionService.saveTransaction(transactionDto);

        String fullName = userToDebit.getFirstName() + " "
                + userToDebit.getLastName()
                + (userToDebit.getOtherName() != null ? " " + userToDebit.getOtherName() : "");

        return BankResponse.builder()
                .responseCode(AccountUtils.ACCOUNT_DEBITED_SUCCESS_CODE)
                .responseMessage(AccountUtils.ACCOUNT_DEBITED_SUCCESS_MESSAGE)
                .accountInfo(AccountInfo.builder()
                        .accountBalance(userToDebit.getAccountBalance())
                        .accountNumber(creditDebitRequest.getAccountNumber())
                        .accountName(fullName)
                        .build())
                .build();
    }

    @Override
    public BankResponse transfer(TransferRequest transferRequest) {

        // Get the account to debit (Check source account exists)
        boolean isSourceAccountExists = userRepository.existsByAccountNumber(transferRequest.getSourceAccountNumber());
        if (!isSourceAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: " + transferRequest.getSourceAccountNumber());
        }

        // Get the account to credit (Check destination account exists)
        boolean isDestinationAccountExists = userRepository.existsByAccountNumber(transferRequest.getDestinationAccountNumber());
        if(!isDestinationAccountExists) {
            throw new ResourceNotFoundException("Account not found with account number: " + transferRequest.getDestinationAccountNumber());
        }

        // Check if the amount debited is not more than the current balance
        User sourceAccount = userRepository.findByAccountNumber(transferRequest.getSourceAccountNumber());

        String sourceAccountUserFullName = sourceAccount.getFirstName() + " "
                + sourceAccount.getLastName()
                    + (sourceAccount.getOtherName() != null ? " " + sourceAccount.getOtherName() : "");

        if(transferRequest.getAmount().compareTo(sourceAccount.getAccountBalance()) > 0) {
            throw new InsufficientBalanceException("Insufficient Balance: "
                    + transferRequest.getAmount());
        }

        // Debit amount
        BigDecimal currentSourceAccountBalance = sourceAccount.getAccountBalance();
        sourceAccount.setAccountBalance(currentSourceAccountBalance.subtract(transferRequest.getAmount()));
        userRepository.save(sourceAccount);

        // Save Transaction
        TransactionDto DebitransactionDto = TransactionDto.builder()
                .accountNumber(sourceAccount.getAccountNumber())
                .transactionType("DEBIT")
                .amount(transferRequest.getAmount())
                .build();

        transactionService.saveTransaction(DebitransactionDto);

        // Send Debit Amount Email Alert
//        EmailDetails debitAlert = EmailDetails.builder()
//                .subject("DEBIT ALERT")
//                .recipient(sourceAccount.getEmail())
//                .messageBody("The sum of " + transferRequest.getAmount() + " has been deducted from you account! Your Current Balance is " + sourceAccount.getAccountBalance())
//                .build();
//
//        emailService.sendEmailAlert(debitAlert);

        // Get the account to credit
        User destinationAccount = userRepository.findByAccountNumber(transferRequest.getDestinationAccountNumber());
        BigDecimal currentDestinationAccountBalance = destinationAccount.getAccountBalance();

        // Credit the amount
        destinationAccount.setAccountBalance(currentDestinationAccountBalance.add(transferRequest.getAmount()));
        userRepository.save(destinationAccount);

        // Save Transaction
        TransactionDto creditTransactionDto = TransactionDto.builder()
                .accountNumber(destinationAccount.getAccountNumber())
                .transactionType("CREDIT")
                .amount(transferRequest.getAmount())
                .build();

        transactionService.saveTransaction(creditTransactionDto);

        // Send Credit Amount Email Alert
//        EmailDetails creditAlert = EmailDetails.builder()
//                .subject("CREDIT ALERT")
//                .recipient(destinationAccount.getEmail())
//                .messageBody("The sum of " + transferRequest.getAmount() + " has been credited to you account from " + sourceAccountUserFullName + ". Your Current Balance is " + destinationAccount.getAccountBalance())
//                .build();
//
//        emailService.sendEmailAlert(creditAlert);

        return BankResponse.builder()
                .responseCode(AccountUtils.TRANSFER_SUCCESS_CODE)
                .responseMessage(AccountUtils.TRANSFER_SUCCESS_MESSAGE)
                .accountInfo(null)
                .build();

    }
}
