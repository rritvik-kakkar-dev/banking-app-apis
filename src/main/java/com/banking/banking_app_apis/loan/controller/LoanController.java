package com.banking.banking_app_apis.loan.controller;

import com.banking.banking_app_apis.dto.CreateLoanRequest;
import com.banking.banking_app_apis.loan.dto.LoanResponse;
import com.banking.banking_app_apis.entity.User;
import com.banking.banking_app_apis.loan.service.LoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
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
    public LoanResponse createLoan(@RequestBody CreateLoanRequest request, @AuthenticationPrincipal User currentUser) {
        return loanService.createLoan(request, currentUser);
    }
}
