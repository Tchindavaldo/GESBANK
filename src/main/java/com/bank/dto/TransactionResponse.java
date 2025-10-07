package com.bank.dto;

import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private TransactionType transactionType;

    private BigDecimal amount;

    private String currency;

    private String description;

    private TransactionStatus status;

    private String referenceNumber;

    private AccountInfo sourceAccount;

    private AccountInfo destinationAccount;

    private BigDecimal sourceBalanceBefore;

    private BigDecimal sourceBalanceAfter;

    private BigDecimal destinationBalanceBefore;

    private BigDecimal destinationBalanceAfter;

    private String failureReason;

    private LocalDateTime transactionDate;

    private LocalDateTime completedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfo {
        private Long id;
        private String accountNumber;
        private String accountType;
        private String ownerName;
    }

    public static TransactionResponse fromTransaction(com.bank.model.Transaction transaction) {
        TransactionResponseBuilder builder = TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .referenceNumber(transaction.getReferenceNumber())
                .sourceBalanceBefore(transaction.getSourceBalanceBefore())
                .sourceBalanceAfter(transaction.getSourceBalanceAfter())
                .destinationBalanceBefore(transaction.getDestinationBalanceBefore())
                .destinationBalanceAfter(transaction.getDestinationBalanceAfter())
                .failureReason(transaction.getFailureReason())
                .transactionDate(transaction.getTransactionDate())
                .completedAt(transaction.getCompletedAt());

        if (transaction.getSourceAccount() != null) {
            builder.sourceAccount(AccountInfo.builder()
                    .id(transaction.getSourceAccount().getId())
                    .accountNumber(transaction.getSourceAccount().getAccountNumber())
                    .accountType(transaction.getSourceAccount().getAccountType().name())
                    .ownerName(transaction.getSourceAccount().getUser().getFullName())
                    .build());
        }

        if (transaction.getDestinationAccount() != null) {
            builder.destinationAccount(AccountInfo.builder()
                    .id(transaction.getDestinationAccount().getId())
                    .accountNumber(transaction.getDestinationAccount().getAccountNumber())
                    .accountType(transaction.getDestinationAccount().getAccountType().name())
                    .ownerName(transaction.getDestinationAccount().getUser().getFullName())
                    .build());
        }

        return builder.build();
    }
}
