package com.banking.banking_app_apis.transaction.service;

import com.banking.banking_app_apis.transaction.dto.TransactionResponse;
import com.itextpdf.text.DocumentException;
import org.springframework.data.domain.Page;

import java.io.FileNotFoundException;

public interface BankService {

    Page<TransactionResponse> generateStatement(
            String accountNumber,
            String startDate,
            String endDate,
            int page,
            int size,
            String sortBy,
            String direction
    ) throws FileNotFoundException, DocumentException;
}
