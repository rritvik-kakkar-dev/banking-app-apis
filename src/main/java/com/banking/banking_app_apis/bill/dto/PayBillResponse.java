package com.banking.banking_app_apis.bill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayBillResponse {

    private String message;
    private BillResponse bill;
    private BillPaymentResponse billPayment;
}
