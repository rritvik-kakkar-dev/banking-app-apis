package com.banking.banking_app_apis.bill.repository;

import com.banking.banking_app_apis.bill.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
}
