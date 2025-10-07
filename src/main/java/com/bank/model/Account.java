package com.bank.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = "account_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le numéro de compte est obligatoire")
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotNull(message = "Le type de compte est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull(message = "Le solde est obligatoire")
    @DecimalMin(value = "0.0", inclusive = true, message = "Le solde ne peut pas être négatif")
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3)
    @Builder.Default
    private String currency = "EUR";

    @NotNull(message = "Le statut du compte est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sourceAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> outgoingTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "destinationAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Transaction> incomingTransactions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Méthodes utilitaires
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être positif");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant du retrait doit être positif");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Solde insuffisant");
        }
        this.balance = this.balance.subtract(amount);
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = AccountStatus.INACTIVE;
        this.closedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }
}
