package com.banking.banking_app_apis.loan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PayEmiRequest {

    private Integer month;

    private Integer year;

    private BigDecimal amount;

    @NotNull
    private Long accountId;
}