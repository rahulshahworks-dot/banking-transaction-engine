package com.rahul.bankingsystem.dto;

import com.rahul.bankingsystem.model.TransactionStatus;
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
public class TransferResponseDTO {
    private UUID transactionKey;
    private String message;
    private TransactionStatus status;
    private BigDecimal transferredAmount;
    private String currency;
}
