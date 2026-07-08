package com.banking.banking_app_apis.service;

import com.banking.banking_app_apis.account.constants.AccountConstants;
import com.banking.banking_app_apis.account.dto.CreditDebitRequest;
import com.banking.banking_app_apis.account.dto.EnquiryRequest;
import com.banking.banking_app_apis.account.dto.TransferRequest;
import com.banking.banking_app_apis.common.dto.BankResponse;
import com.banking.banking_app_apis.common.exception.DuplicateAccountException;
import com.banking.banking_app_apis.common.exception.InsufficientBalanceException;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.notification.dto.EmailDetails;
import com.banking.banking_app_apis.notification.service.EmailService;
import com.banking.banking_app_apis.transaction.service.TransactionService;
import com.banking.banking_app_apis.user.dto.UpdateUserRequest;
import com.banking.banking_app_apis.user.entity.Role;
import com.banking.banking_app_apis.user.entity.User;
import com.banking.banking_app_apis.user.repository.UserRepository;
import com.banking.banking_app_apis.user.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private PasswordEncoder passwordEncoder;


    /**
     * balanceEnquiry() test Cases
     */
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

        assertThat(response.getResponseCode()).isEqualTo(AccountConstants.ACCOUNT_FOUND_CODE);
        assertThat(response.getAccountSummaryResponse().getAccountBalance()).isEqualTo(BigDecimal.valueOf(50000));
    }


    /**
     * creditAmount() test Cases
     */
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

        assertThat(response.getResponseCode()).isEqualTo(AccountConstants.ACCOUNT_CREDITED_SUCCESS_CODE);
        assertThat(mockUser.getAccountBalance()).isEqualTo(BigDecimal.valueOf(60000));
        verify(userRepository, times(1)).save(any(User.class));
    }


    /**
     * debitAmount() test Cases Start
     */
    @Test
    void shouldReturnNotFoundExceptionWhenDoesNotExists() {

        // GIVEN
        CreditDebitRequest request = new CreditDebitRequest();
        request.setAccountNumber("1234567890");
        request.setAmount(BigDecimal.valueOf(10000));

        when(userRepository.existsByAccountNumber("1234567890")).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.debitAmount(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1234567890");
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenAmountExceedsBalance() {

        // GIVEN
        CreditDebitRequest request = new CreditDebitRequest();
        request.setAccountNumber("2026316384");
        request.setAmount(BigDecimal.valueOf(1000));

        when(userRepository.existsByAccountNumber("2026316384")).thenReturn(true);

        User mockUser = User.builder()
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.valueOf(500))
                .firstName("Rritvik")
                .lastName("Kakkar")
                .build();

        when(userRepository.findByAccountNumber("2026316384")).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.debitAmount(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient Balance: "
                        + request.getAmount());
    }

    @Test
    void shouldDebitAmountWhenAccountExists() {
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

        BankResponse response = userService.debitAmount(request);

        assertThat(response.getResponseCode()).isEqualTo(AccountConstants.ACCOUNT_DEBITED_SUCCESS_CODE);
        assertThat(mockUser.getAccountBalance()).isEqualTo(BigDecimal.valueOf(40000));
        verify(userRepository, times(1)).save(any(User.class));
    }


    /**
     * transfer() test Cases
     */
    @Test
    void shouldThrowNotFoundExceptionWhenSourceAccountNotExists() {
        //GIVEN
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("1234567890");

        when(userRepository.existsByAccountNumber(request.getSourceAccountNumber())).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.transfer(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(request.getSourceAccountNumber());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDestinationAccountNotExists() {
        //GIVEN
        TransferRequest request = new TransferRequest();
        request.setDestinationAccountNumber("1234567890");

        when(userRepository.existsByAccountNumber(request.getDestinationAccountNumber())).thenReturn(false);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.transfer(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(request.getDestinationAccountNumber());
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenAmountExceedsBalanceSourceAccount() {
        // GIVEN
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("2026316384");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setDestinationAccountNumber("2026470375");

        when(userRepository.existsByAccountNumber(request.getSourceAccountNumber())).thenReturn(true);
        when(userRepository.existsByAccountNumber(request.getDestinationAccountNumber())).thenReturn(true);

        User mockUser = User.builder()
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.valueOf(500))
                .firstName("Rritvik")
                .lastName("Kakkar")
                .build();

        when(userRepository.findByAccountNumber(request.getSourceAccountNumber())).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.transfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient Balance: "
                        + request.getAmount());
    }

    @Test
    void shouldTransferAmountWhenAccountsExists() {
        //GIVEN
        TransferRequest request = new TransferRequest();
        request.setSourceAccountNumber("2026316384");
        request.setAmount(BigDecimal.valueOf(1000));
        request.setDestinationAccountNumber("2026470375");

        when(userRepository.existsByAccountNumber(request.getSourceAccountNumber())).thenReturn(true);
        when(userRepository.existsByAccountNumber(request.getDestinationAccountNumber())).thenReturn(true);

        User mockUser = User.builder()
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.valueOf(50000))
                .firstName("Rritvik")
                .lastName("Kakkar")
                .build();

        when(userRepository.findByAccountNumber(request.getSourceAccountNumber())).thenReturn(mockUser);

        User mockDestinationUser = User.builder()
                .accountNumber("2026470375")
                .accountBalance(BigDecimal.valueOf(20000))
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.findByAccountNumber(request.getDestinationAccountNumber())).thenReturn(mockDestinationUser);

        BankResponse response = userService.transfer(request);

        assertThat(response.getResponseCode()).isEqualTo(AccountConstants.TRANSFER_SUCCESS_CODE);
        assertThat(mockUser.getAccountBalance()).isEqualTo(BigDecimal.valueOf(49000));
        verify(userRepository, times(2)).save(any(User.class));
        assertThat(mockDestinationUser.getAccountBalance()).isEqualTo(BigDecimal.valueOf(21000));
    }


    /**
     * createAccount() test Cases
     */
    @Test
    void shouldThrowDuplicateAccountExceptionWhenEmailExists() {
        // GIVEN
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("rritvik98kakkar@gmail.com");

        when(userRepository.existsByEmail("rritvik98kakkar@gmail.com")).thenReturn(true);

        // WHEN + THEN
        assertThatThrownBy(() -> userService.createAccount(request))
                .isInstanceOf(DuplicateAccountException.class)
                .hasMessageContaining("Account with this email already exists: " + request.getEmail());
    }

    @Test
    void shouldCreateUserAndSendEmailWhenEmailDoesNotExists() {
        // GIVEN
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("rritvik981kakkar@gmail.com");

        when(userRepository.existsByEmail("rritvik981kakkar@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        User mockUser = User.builder()
                .firstName("Rritvik")
                .lastName("Kakkar")
                .email("rritvik981kakkar@gmail.com")
                .accountNumber("2026316384")
                .accountBalance(BigDecimal.ZERO)
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // WHEN
        BankResponse response = userService.createAccount(request);

        // THEN
        assertThat(response.getResponseCode()).isEqualTo(AccountConstants.ACCOUNT_CREATION_SUCCESS_CODE);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendEmailAlert(any(EmailDetails.class));
    }
}