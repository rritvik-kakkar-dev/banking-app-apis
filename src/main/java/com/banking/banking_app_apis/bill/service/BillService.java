package com.banking.banking_app_apis.bill.service;

import com.banking.banking_app_apis.bill.dto.*;
import com.banking.banking_app_apis.user.entity.User;
import org.springframework.data.domain.Page;

public interface BillService {

    BillResponse createBill(BillRequest request, User user);

    Page<BillResponse> getBills(User user, int page, int limit, String sortBy, String sortOrder, Long accountId);

    PayBillResponse payBills(Long billId, PayBillRequest request, User user);

    Page<BillPaymentResponse> getBillPayments(User user, int page, int limit, String sortBy, String sortOrder, Long billId);

    BillResponse toggleAutoPay(User user, Long billId);
}
