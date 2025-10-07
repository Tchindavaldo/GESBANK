package com.bank.dto;

import com.bank.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull(message = "Le type de compte est obligatoire")
    private AccountType accountType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Le solde initial ne peut pas être négatif")
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Size(min = 3, max = 3, message = "Le code devise doit contenir 3 caractères")
    @Builder.Default
    private String currency = "EUR";

    @Size(max = 200, message = "La description ne peut pas dépasser 200 caractères")
    private String description;
}
