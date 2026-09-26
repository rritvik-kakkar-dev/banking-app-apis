package com.banking.banking_app_apis.bill.mapper;

import com.banking.banking_app_apis.bill.dto.BillPaymentResponse;
import com.banking.banking_app_apis.bill.entity.BillPayment;
import org.springframework.stereotype.Component;

@Component
public class BillPaymentMapper {

    public BillPaymentResponse toBillPaymentResponse(BillPayment billPayment) {
        return BillPaymentResponse.builder()
                .id(billPayment.getId())
                .amount(billPayment.getAmount())
                .paidAt(billPayment.getPaidAt())
                .pendingAmount(billPayment.getPendingAmount())
                .createdAt(billPayment.getCreatedAt())
                .build();
    }
}
