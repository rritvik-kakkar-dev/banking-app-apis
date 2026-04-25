package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.BankResponse;
import com.banking.banking_app_apis.dto.EnquiryRequest;
import com.banking.banking_app_apis.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.repository.UserRepository;
import com.banking.banking_app_apis.utils.AccountUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

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

}