package com.banking.banking_app_apis.bill.service;

import com.banking.banking_app_apis.bill.dto.BillRequest;
import com.banking.banking_app_apis.bill.dto.BillResponse;
import com.banking.banking_app_apis.user.entity.User;

public interface BillService {

    BillResponse createBill(BillRequest request, User user);
}
