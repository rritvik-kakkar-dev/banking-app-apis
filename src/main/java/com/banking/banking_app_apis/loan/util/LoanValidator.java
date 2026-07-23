package com.banking.banking_app_apis.loan.util;

import com.banking.banking_app_apis.common.exception.ValidationException;
import com.banking.banking_app_apis.loan.dto.CreateLoanRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class LoanValidator {
    public void validate(CreateLoanRequest request) {
        if (request.getPrincipal().compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException("Principal must be greater than zero.");
        if (request.getAnnualInterest().compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Annual interest cannot be negative.");
        if (request.getTenure() <= 0)
            throw new ValidationException("Tenure must be at least 1 year.");
        if (request.getStartDate().isBefore(LocalDate.now()))
            throw new ValidationException("Start date cannot be in the past.");
    }
}
