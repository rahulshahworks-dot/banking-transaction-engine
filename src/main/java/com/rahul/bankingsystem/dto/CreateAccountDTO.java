package com.rahul.bankingsystem.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateAccountDTO {

    @NotBlank
    @Size(max = 3)
    private String currency = "USD";

    @DecimalMin(value = "0.00")
    private BigDecimal openingBalance = BigDecimal.ZERO;
}
