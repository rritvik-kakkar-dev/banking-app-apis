package com.banking.banking_app_apis.bill.service;

import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.bill.dto.*;
import com.banking.banking_app_apis.bill.entity.Bill;
import com.banking.banking_app_apis.bill.entity.BillPayment;
import com.banking.banking_app_apis.bill.entity.BillStatus;
import com.banking.banking_app_apis.bill.mapper.BillMapper;
import com.banking.banking_app_apis.bill.mapper.BillPaymentMapper;
import com.banking.banking_app_apis.bill.repository.BillPaymentRepository;
import com.banking.banking_app_apis.bill.repository.BillRepository;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillServieImpl implements BillService{

    private final AccountRepository accountRepository;
    private final BillRepository billRepository;
    private final BillPaymentRepository billPaymentRepository;
    private final BillMapper billMapper;
    private final BillPaymentMapper billPaymentMapper;

    @Override
    public BillResponse createBill(BillRequest request, User user) {
        Long accountId = request.getAccountId();

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        Bill bill = Bill.builder()
                .billName(request.getBillName())
                .billerName(request.getBillerName())
                .billAmount(request.getAmount())
                .description(request.getDescription())
                .autopayEnabled(request.getAutoPayEnable())
                .status(BillStatus.PENDING)
                .monthlyDueDate(request.getMonthlyDueDate())
                .billPeriod(request.getBillPeriod())
                .account(account)
                .build();

        billRepository.save(bill);

        return billMapper.toBillResponse(bill);
    }

    @Override
    public Page<BillResponse> getBills(User user, int page, int limit, String sortBy, String sortOrder, Long accountId) {
        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, limit, sort);

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        return billRepository.findByAccountId(accountId, pageable)
                .map(billMapper::toBillResponse);
    }

    @Override
    public PayBillResponse payBills(Long billId, PayBillRequest request, User user) {

        Account account = accountRepository.findById(request.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Account Not found with ID: " + request.getAccountId()));

        Bill bill = billRepository.findById(billId).orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(
                LocalDate.now().lengthOfMonth()
        );

        BillPayment billPaymentRecord =
                billPaymentRepository.findByBillAndPaidAtBetween(
                        bill,
                        startOfMonth,
                        endOfMonth
                );

        if(!bill.getAccount().getId().equals(account.getId())) {
            throw new ValidationException("Bill does not belong to this account!");
        }

        if(billPaymentRecord != null && billPaymentRecord.getPaidAt().getMonth().equals(LocalDate.now().getMonth())) {
            throw new ValidationException("Bill for this month is already paid at: " + billPaymentRecord.getModifiedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        }

        BigDecimal pendingAmount = bill.getBillAmount().subtract(request.getAmount());
        String message;

        bill.setBillPaidAt(LocalDate.now());

        if(!pendingAmount.equals(BigDecimal.ZERO)) {
            message = "Partial Bill Payment made for month: " + LocalDate.now().getMonth();
            bill.setStatus(BillStatus.PARTIAL_PAYMENT);
        } else {
            message = "Bill fully paid for month: " + LocalDate.now().getMonth();
            bill.setStatus(BillStatus.PAID);
        }

        billRepository.save(bill);

        BillPayment billPayment = new BillPayment();
        billPayment.setBill(bill);
        billPayment.setAmount(request.getAmount());
        billPayment.setPaidAt(LocalDate.now());
        billPayment.setPendingAmount(pendingAmount);
        billPaymentRepository.save(billPayment);

        return PayBillResponse.builder()
                .message(message)
                .bill(
                        BillResponse.builder()
                                .id(bill.getId())
                                .billName(bill.getBillName())
                                .billerName(bill.getBillerName())
                                .autoPayEnabled(bill.getAutopayEnabled())
                                .amount(bill.getBillAmount())
                                .paidAt(bill.getBillPaidAt())
                                .status(BillStatus.PAID)
                                .monthlyDueDate(bill.getMonthlyDueDate())
                                .billPeriod(bill.getBillPeriod())
                                .accountNumber(account.getAccountNumber())
                                .accountName(account.getAccountName())
                                .createdAt(bill.getCreatedAt())
                                .build()
                )
                .billPayment(
                        BillPaymentResponse.builder()
                                .id(billPayment.getId())
                                .amount(billPayment.getAmount())
                                .paidAt(billPayment.getPaidAt())
                                .pendingAmount(billPayment.getPendingAmount())
                                .createdAt(billPayment.getCreatedAt())
                                .build()
                )
                .build();
    }

    @Override
    public Page<BillPaymentResponse> getBillPayments(User user, int page, int limit, String sortBy, String sortOrder, Long billId) {
        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, limit, sort);

        Bill bill = billRepository.findById(billId).orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        return billPaymentRepository.findByBillId(billId, pageable)
                .map(billPaymentMapper::toBillPaymentResponse);
    }

    @Override
    public BillResponse toggleAutoPay(User user, Long billId) {
        Bill bill = billRepository.findById(billId).orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));

        Account account = accountRepository.findById(bill.getAccount().getId()).orElseThrow(() -> new ResourceNotFoundException("Account Not found with ID: " + bill.getAccount().getId()));

        if(bill.getAutopayEnabled().equals(1)) {
            bill.setAutopayEnabled(0);
        } else if(bill.getAutopayEnabled().equals(0)) {
            bill.setAutopayEnabled(1);
        }

        billRepository.save(bill);

        return BillResponse.builder()
                .id(bill.getId())
                .billName(bill.getBillName())
                .autoPayEnabled(bill.getAutopayEnabled())
                .billerName(bill.getBillerName())
                .amount(bill.getBillAmount())
                .paidAt(bill.getBillPaidAt())
                .status(BillStatus.PAID)
                .monthlyDueDate(bill.getMonthlyDueDate())
                .billPeriod(bill.getBillPeriod())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}
