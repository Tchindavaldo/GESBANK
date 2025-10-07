package com.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_transaction_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le type de transaction est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "EUR";

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Le statut de la transaction est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id")
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id")
    private Account destinationAccount;

    @Column(name = "source_balance_before", precision = 15, scale = 2)
    private BigDecimal sourceBalanceBefore;

    @Column(name = "source_balance_after", precision = 15, scale = 2)
    private BigDecimal sourceBalanceAfter;

    @Column(name = "destination_balance_before", precision = 15, scale = 2)
    private BigDecimal destinationBalanceBefore;

    @Column(name = "destination_balance_after", precision = 15, scale = 2)
    private BigDecimal destinationBalanceAfter;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Méthodes utilitaires
    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
    }

    public void cancel() {
        this.status = TransactionStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return TransactionStatus.COMPLETED.equals(this.status);
    }

    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }

    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }
}
