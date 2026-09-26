package com.banking.banking_app_apis.bill.controller;

import com.banking.banking_app_apis.bill.dto.*;
import com.banking.banking_app_apis.bill.service.BillService;
import com.banking.banking_app_apis.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BillResponse>> getBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam Long accountId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                billService.getBills(
                        currentUser,
                        page,
                        limit,
                        sortBy,
                        sortOrder,
                        accountId
                )
        );
    }


    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PayBillResponse> payBill(@PathVariable Long id, @RequestBody PayBillRequest request, @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                billService.payBills(id, request, currentUser)
        );
    }

    @GetMapping("/{id}/billPayments")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BillPaymentResponse>> getBillPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                billService.getBillPayments(
                        currentUser,
                        page,
                        limit,
                        sortBy,
                        sortOrder,
                        id
                )
        );
    }

    @PutMapping("/{id}/autopay")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BillResponse> toggleAutoPay(@AuthenticationPrincipal User currentUser, @PathVariable Long id) {
        return ResponseEntity.ok(billService.toggleAutoPay(currentUser, id));
    }

}
