package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.*;
import com.banking.banking_app_apis.entity.User;
import com.banking.banking_app_apis.repository.UserRepository;
import com.banking.banking_app_apis.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Creating an account - saving a new user into the db
     * Check if user already has an account
     */
    @Override
    public BankResponse createAccount(UserRequest userRequest) {

        if(userRepository.existsByEmail(userRequest.getEmail())) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
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
                .phoneNumber(userRequest.getPhoneNumber())
                .alternativePhoneNumber(userRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
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
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
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
            return AccountUtils.ACCOUNT_FOUND_MESSAGE;
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
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
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
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        // Check if amount you intent to withdraw is not more than the current account balance
        User userToDebit = userRepository.findByAccountNumber(creditDebitRequest.getAccountNumber());
        BigDecimal currentBalance = userToDebit.getAccountBalance();

        BigInteger availableBalance = userToDebit.getAccountBalance().toBigInteger();
        BigInteger debitAmount = creditDebitRequest.getAmount().toBigInteger();

        if(availableBalance.intValue() < debitAmount.intValue()) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
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

        // Get the account to debit (Check destination account exists)
        boolean isDestinationAccountExists = userRepository.existsByAccountNumber(transferRequest.getDestinationAccountNumber());
        if(!isDestinationAccountExists) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.ACCOUNT_NOT_EXISTS_CODE)
                    .responseMessage(AccountUtils.ACCOUNT_NOT_EXISTS_MESSAGE)
                    .accountInfo(null)
                    .build();
        }

        // Check if the amount debited is not more than the current balance
        User sourceAccount = userRepository.findByAccountNumber(transferRequest.getSourceAccountNumber());

        String sourceAccountUserFullName = sourceAccount.getFirstName() + " "
                + sourceAccount.getLastName()
                    + (sourceAccount.getOtherName() != null ? " " + sourceAccount.getOtherName() : "");

        if(transferRequest.getAmount().compareTo(sourceAccount.getAccountBalance()) > 0) {
            return BankResponse.builder()
                    .responseCode(AccountUtils.INSUFFICIENT_BALANCE_CODE)
                    .responseMessage(AccountUtils.INSUFFICIENT_BALANCE_MESSAGE)
                    .accountInfo(null)
                    .build();
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
