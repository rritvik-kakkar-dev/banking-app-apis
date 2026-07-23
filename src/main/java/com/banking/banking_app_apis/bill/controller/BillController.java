package com.banking.banking_app_apis.bill.controller;

import com.banking.banking_app_apis.bill.dto.BillRequest;
import com.banking.banking_app_apis.bill.dto.BillResponse;
import com.banking.banking_app_apis.bill.service.BillService;
import com.banking.banking_app_apis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public BillResponse createBill(@RequestBody BillRequest request, @AuthenticationPrincipal User currentUser) {
        return billService.createBill(request, currentUser);
    }
}
