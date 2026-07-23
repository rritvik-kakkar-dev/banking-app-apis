package com.banking.banking_app_apis.bill.dto;

import com.banking.banking_app_apis.bill.entity.BillPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillRequest {

    public String billName;
    public String billerName;
    public String description;
    public BigDecimal amount;
    public Integer autoPayEnable;
    private Integer monthlyDueDate;
    private BillPeriod billPeriod;
    public Long accountId;

}
