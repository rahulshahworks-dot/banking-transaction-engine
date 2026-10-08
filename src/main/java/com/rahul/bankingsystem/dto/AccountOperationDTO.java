package com.rahul.bankingsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AccountOperationDTO {

    @DecimalMin(value = "0.01")
    private BigDecimal amount;
}
