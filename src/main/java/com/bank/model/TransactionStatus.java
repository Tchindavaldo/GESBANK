package com.bank.model;

public enum TransactionStatus {
    PENDING,      // Transaction en attente
    PROCESSING,   // Transaction en cours de traitement
    COMPLETED,    // Transaction complétée
    FAILED,       // Transaction échouée
    CANCELLED,    // Transaction annulée
    REVERSED      // Transaction inversée/remboursée
}
