package com.banking.banking_app_apis.account.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;

@Component
public class AccountNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generateAccountNumber() {
        int year = Year.now().getValue();
        int randomNumber = 100000 + random.nextInt(900000);

        return "%d%06d".formatted(year, randomNumber);
    }
}
