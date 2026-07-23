package com.banking.banking_app_apis.bill.mapper;

import com.banking.banking_app_apis.bill.dto.BillResponse;
import com.banking.banking_app_apis.bill.entity.Bill;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {

    public BillResponse toBillResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billName(bill.getBillName())
                .billerName(bill.getBillerName())
                .amount(bill.getBillAmount())
                .billPeriod(bill.getBillPeriod())
                .status(bill.getStatus())
                .paidAt(bill.getBillPaidAt())
                .accountNumber(bill.getAccount().getAccountNumber())
                .accountName(bill.getAccount().getAccountName())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}
