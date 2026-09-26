package com.banking.banking_app_apis.bill.repository;

import com.banking.banking_app_apis.bill.entity.Bill;
import com.banking.banking_app_apis.bill.entity.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {

    BillPayment findByBillAndPaidAtBetween(Bill bill, LocalDate startOfMonth, LocalDate endOfMonth);

    Page<BillPayment> findByBillId(Long billId, Pageable pageable);

}
