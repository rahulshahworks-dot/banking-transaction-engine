package com.rahul.bankingsystem.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCompletedEvent {
    private UUID transactionKey;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String currency;
    private String status;
}
