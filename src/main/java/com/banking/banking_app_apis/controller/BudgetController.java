package com.banking.banking_app_apis.controller;

import com.banking.banking_app_apis.service.BudgetService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budget")
@AllArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
}
