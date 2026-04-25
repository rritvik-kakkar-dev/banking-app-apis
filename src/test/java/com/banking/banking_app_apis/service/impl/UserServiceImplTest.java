package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.BankResponse;
import com.banking.banking_app_apis.dto.CreditDebitRequest;
import com.banking.banking_app_apis.dto.EnquiryRequest;
import com.banking.banking_app_apis.entity.User;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.repository.UserRepository;
import com.banking.banking_app_apis.utils.AccountUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnAccountNotFoundWhenAccountDoesNotExist() {

        // GIVEN
        EnquiryRequest request = new EnquiryRequest();
        request.setAccountNumber("1234567890");

        when(userRepository.existsByAccountNumber("1234567890")).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.balanceEnquiry(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1234567890");
    }


    @Test
    void shouldReturnBankResponseWhenAccountExists() {

        // GIVEN
        EnquiryRequest request = new EnquiryRequest();
        request.setAccountNumber("2026316384");

        when(userRepository.existsByAccountNumber("2026316384")).thenReturn(true);

        User mockUser = User.builder()
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.valueOf(50000))
                .firstName("Rritvik")
                .lastName("Kakkar")
                .build();

        when(userRepository.findByAccountNumber("2026316384")).thenReturn(mockUser);

        // WHEN
        BankResponse response = userService.balanceEnquiry(request);

        assertThat(response.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_FOUND_CODE);
        assertThat(response.getAccountInfo().getAccountBalance()).isEqualTo(BigDecimal.valueOf(50000));
    }


    @Test
    void shouldReturnAccountNotFoundWhenAmountCredit() {

        //GIVEN
        EnquiryRequest request = new EnquiryRequest();
        request.setAccountNumber("1234567890");

        when(userRepository.existsByAccountNumber("1234567890")).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.balanceEnquiry(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1234567890");
    }


    @Test
    void shouldCreditAmountWhenAccountExists() {
        //GIVEN
        CreditDebitRequest request = new CreditDebitRequest();
        request.setAccountNumber("2026316384");
        request.setAmount(BigDecimal.valueOf(10000));

        when(userRepository.existsByAccountNumber("2026316384")).thenReturn(true);

        User mockUser = User.builder()
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.valueOf(50000))
                .firstName("Rritvik")
                .lastName("Kakkar")
                .build();

        when(userRepository.findByAccountNumber("2026316384")).thenReturn(mockUser);

        BankResponse response = userService.creditAmount(request);

        assertThat(response.getResponseCode()).isEqualTo(AccountUtils.ACCOUNT_CREDITED_SUCCESS_CODE);
        assertThat(mockUser.getAccountBalance()).isEqualTo(BigDecimal.valueOf(60000));
        verify(userRepository, times(1)).save(any(User.class));
    }

}