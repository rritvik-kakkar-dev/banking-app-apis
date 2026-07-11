package com.banking.banking_app_apis.loan.controller;

import com.banking.banking_app_apis.loan.dto.*;
import com.banking.banking_app_apis.loan.service.LoanService;
import com.banking.banking_app_apis.user.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@AllArgsConstructor
@Tag(name = "Loan APIs")
public class LoanController {

    private LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public LoanDetailResponse createLoan(@RequestBody CreateLoanRequest request, @AuthenticationPrincipal User currentUser) {
        return loanService.createLoan(request, currentUser);
    }

    @GetMapping
    public ResponseEntity<Page<LoanSummaryResponse>> getLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                loanService.getLoans(
                        currentUser,
                        page,
                        limit,
                        sortBy,
                        sortOrder
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public LoanDetailResponse getLoan(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return loanService.getLoan(id, currentUser);
    }

    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<LoanScheduleDetailsResponse> getLoanSchedule(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
                loanService.getLoanSchedule(
                        id,
                        currentUser
                )
        );
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PayEmiResponse> payEmi(
            @PathVariable Long id,
            @Valid @RequestBody PayEmiRequest request,
            @AuthenticationPrincipal User currentUser) {

        PayEmiResponse response = loanService.payEmi(id, request, currentUser);
        return ResponseEntity.ok(response);
    }


}
