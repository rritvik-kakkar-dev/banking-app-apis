package com.banking.banking_app_apis.controller;

import com.banking.banking_app_apis.dto.TransactionDto;
import com.banking.banking_app_apis.service.TransactionService;
import com.banking.banking_app_apis.service.impl.BankStatement;
import com.itextpdf.text.DocumentException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

@RestController
@RequestMapping("/api/bankStatement")
@AllArgsConstructor
@Tag(name = "Bank Statement APIs")
public class TransactionController {

    private BankStatement bankStatement;

    private TransactionService transactionService;

    @GetMapping
    public Page<TransactionDto> generateBankStatement(@RequestParam String accountNumber,
                                                      @RequestParam String startDate,
                                                      @RequestParam String endDate, @RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue = "10") String size, @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(defaultValue = "DESC") String direction) throws DocumentException, FileNotFoundException {
        return bankStatement.generateStatement(accountNumber, startDate, endDate, Integer.parseInt(page), Integer.parseInt(size), sortBy, direction);
    }

    @GetMapping("/transaction-history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Page<TransactionDto> getTransactionHistory(
            @RequestParam String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        return transactionService.transactionHistory(accountNumber, page, limit, sortBy, sortOrder);
    }
}
