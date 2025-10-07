package com.bank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class TransactionRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;

    // Pour les virements
    @NotBlank(message = "Le numéro de compte destination est obligatoire", groups = TransferValidation.class)
    private String destinationAccountNumber;

    // Interface marker pour validation de groupe
    public interface TransferValidation {}

    public interface DepositValidation {}

    public interface WithdrawValidation {}
}
