package com.banking.banking_app_apis.user.service;

import com.banking.banking_app_apis.account.constants.AccountConstants;
import com.banking.banking_app_apis.account.dto.AccountSummaryResponse;
import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.service.AccountService;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.common.exception.DuplicateAccountException;
import com.banking.banking_app_apis.notification.dto.EmailDetails;
import com.banking.banking_app_apis.notification.service.EmailService;
import com.banking.banking_app_apis.security.JwtTokenProvider;
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

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    EmailService emailService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AccountService accountService;


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

    @Override
    public BankResponse register(UpdateUserRequest updateUserRequest) {
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

        Account account = accountService.createDefaultAccount(newUser);

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


}
