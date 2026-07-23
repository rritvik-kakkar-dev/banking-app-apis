package com.banking.banking_app_apis.bill.service;

import com.banking.banking_app_apis.account.entity.Account;
import com.banking.banking_app_apis.account.repository.AccountRepository;
import com.banking.banking_app_apis.bill.dto.BillRequest;
import com.banking.banking_app_apis.bill.dto.BillResponse;
import com.banking.banking_app_apis.bill.entity.Bill;
import com.banking.banking_app_apis.bill.entity.BillStatus;
import com.banking.banking_app_apis.bill.mapper.BillMapper;
import com.banking.banking_app_apis.bill.repository.BillRepository;
import com.banking.banking_app_apis.common.exception.ResourceNotFoundException;
import com.banking.banking_app_apis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillServieImpl implements BillService{

    private final AccountRepository accountRepository;
    private final BillRepository billRepository;
    private final BillMapper billMapper;

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
}
