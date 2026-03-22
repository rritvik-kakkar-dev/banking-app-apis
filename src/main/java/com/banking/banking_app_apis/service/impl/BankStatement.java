package com.banking.banking_app_apis.service.impl;

import com.banking.banking_app_apis.dto.EmailDetails;
import com.banking.banking_app_apis.entity.Transaction;
import com.banking.banking_app_apis.entity.User;
import com.banking.banking_app_apis.repository.TransactionRepository;
import com.banking.banking_app_apis.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class BankStatement {

    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private EmailService emailService;

    private static final String FILE = "/Users/yomama/Documents/Spring Boot/Banking App Pdfs/MyStatement.pdf";

    /**
     * 1. retrieve list of transactions within a date range given an account number
     * 2. generate a pdf file of transactions
     * 3. send the file via email
     */

    public List<Transaction> generateStatement(String accountNumber, String startDate, String endDate) throws FileNotFoundException, DocumentException {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);

        User userAccount = userRepository.findByAccountNumber(accountNumber);
        String customerName = userAccount.getFirstName() + " "
                + userAccount.getLastName()
                + (userAccount.getOtherName() != null ? " " + userAccount.getOtherName() : "");

        List<Transaction> transactionList = transactionRepository.findAll().stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .filter(t -> !t.getCreatedAt().isBefore(start))     // >= startDate
                .filter(t -> !t.getCreatedAt().isAfter(end))        // <= endDate
                .sorted(Comparator.comparing(Transaction::getCreatedAt))
                .toList();


        Rectangle statementSize = new Rectangle(PageSize.A4);
        Document document = new Document(statementSize);

        log.info("Setting size of document");

        OutputStream outputStream = new FileOutputStream(FILE);

        PdfWriter.getInstance(document, outputStream);

        document.open();

        // HEADING TABLE 1
        PdfPTable bankInfoTable = new PdfPTable(1);
        PdfPCell bankName = new PdfPCell(new Phrase("Banking App"));
        bankName.setBorder(0);
        bankName.setBackgroundColor(BaseColor.BLUE);
        bankName.setPadding(10f);

        PdfPCell bankAddress = new PdfPCell(new Phrase("23, Dummy Banking Address Street, India"));
        bankAddress.setBorder(0);

        bankInfoTable.addCell(bankName);
        bankInfoTable.addCell(bankAddress);


        // HEADING TABLE 2
        PdfPTable statementInfoTable = new PdfPTable(2);
        PdfPCell customerInfo = new PdfPCell(new Phrase("Start Date: " + startDate));
        customerInfo.setBorder(0);

        PdfPCell statement = new PdfPCell(new Phrase("STATEMENT OF ACCOUNT"));
        statement.setBorder(0);

        PdfPCell stopDate = new PdfPCell(new Phrase("End Date: " + endDate));
        stopDate.setBorder(0);

        PdfPCell name = new PdfPCell(new Phrase("Customer Name: " + customerName));
        name.setBorder(0);

        PdfPCell space = new PdfPCell();
        space.setBorder(0);

        PdfPCell address = new PdfPCell(new Phrase("Customer Address: " + userAccount.getAddress()));
        address.setBorder(0);

        statementInfoTable.addCell(customerInfo);
        statementInfoTable.addCell(statement);
        statementInfoTable.addCell(stopDate);
        statementInfoTable.addCell(name);
        statementInfoTable.addCell(space);
        statementInfoTable.addCell(address);


        // TRANSACTIONS TABLE
        PdfPTable transactionsTable = new PdfPTable(4);
        PdfPCell date = new PdfPCell(new Phrase("DATE"));
        date.setBackgroundColor(BaseColor.BLUE);
        date.setBorder(0);

        PdfPCell transactionType = new PdfPCell(new Phrase("TRANSACTION TYPE"));
        transactionType.setBackgroundColor(BaseColor.BLUE);
        transactionType.setBorder(0);

        PdfPCell transactionAmount = new PdfPCell(new Phrase("TRANSACTION AMOUNT"));
        transactionAmount.setBackgroundColor(BaseColor.BLUE);
        transactionAmount.setBorder(0);

        PdfPCell transactionStatus = new PdfPCell(new Phrase("TRANSACTION STATUS"));
        transactionStatus.setBackgroundColor(BaseColor.BLUE);
        transactionStatus.setBorder(0);

        transactionsTable.addCell(date);
        transactionsTable.addCell(transactionType);
        transactionsTable.addCell(transactionAmount);
        transactionsTable.addCell(transactionStatus);

        transactionList.forEach(transaction -> {
            transactionsTable.addCell(new Phrase(transaction.getCreatedAt().toString()));
            transactionsTable.addCell(new Phrase(transaction.getTransactionType()));
            transactionsTable.addCell(new Phrase(transaction.getAmount().toString()));
            transactionsTable.addCell(new Phrase(transaction.getStatus()));
        });


        document.add(bankInfoTable);
        document.add(statementInfoTable);
        document.add(transactionsTable);

        document.close();


        // Send Statement as an email attachment
        EmailDetails emailDetails = EmailDetails.builder()
                .recipient(userAccount.getEmail())
                .subject("STATEMENT OF ACCOUNT")
                .messageBody("Kindly find your requested account statement attached!")
                .attachment(FILE)
                .build();

        emailService.sendEMailWithAttachment(emailDetails);


        return transactionList;
    }
}
