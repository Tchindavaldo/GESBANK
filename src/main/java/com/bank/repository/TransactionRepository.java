package com.bank.repository;

import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Trouve une transaction par son numéro de référence
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    /**
     * Trouve toutes les transactions d'un compte source
     */
    List<Transaction> findBySourceAccountId(Long accountId);

    /**
     * Trouve toutes les transactions d'un compte destination
     */
    List<Transaction> findByDestinationAccountId(Long accountId);

    /**
     * Trouve toutes les transactions liées à un compte (source ou destination)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);

    /**
     * Trouve toutes les transactions liées à un compte avec pagination
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Trouve les transactions d'un utilisateur (tous ses comptes)
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findAllByUserId(@Param("userId") Long userId);

    /**
     * Trouve les transactions d'un utilisateur avec pagination
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId ORDER BY t.transactionDate DESC")
    Page<Transaction> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Trouve les transactions par type
     */
    List<Transaction> findByTransactionType(TransactionType transactionType);

    /**
     * Trouve les transactions par statut
     */
    List<Transaction> findByStatus(TransactionStatus status);

    /**
     * Trouve les transactions par type et statut
     */
    List<Transaction> findByTransactionTypeAndStatus(TransactionType transactionType, TransactionStatus status);

    /**
     * Trouve les transactions dans une période donnée
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve les transactions d'un compte dans une période donnée
     */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve les transactions d'un compte dans une période avec pagination
     */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Trouve les transactions après une certaine date
     */
    List<Transaction> findByTransactionDateAfter(LocalDateTime date);

    /**
     * Trouve les transactions avant une certaine date
     */
    List<Transaction> findByTransactionDateBefore(LocalDateTime date);

    /**
     * Trouve les transactions avec un montant supérieur à un seuil
     */
    List<Transaction> findByAmountGreaterThan(BigDecimal amount);

    /**
     * Trouve les transactions en attente
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' ORDER BY t.transactionDate ASC")
    List<Transaction> findPendingTransactions();

    /**
     * Trouve les transactions échouées
     */
    @Query("SELECT t FROM Transaction t WHERE t.status = 'FAILED' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactions();

    /**
     * Calcule le total des transactions pour un compte
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sourceAccount.id = :accountId AND t.status = 'COMPLETED'")
    BigDecimal getTotalOutgoingAmount(@Param("accountId") Long accountId);

    /**
     * Calcule le total des transactions entrantes pour un compte
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.destinationAccount.id = :accountId AND t.status = 'COMPLETED'")
    BigDecimal getTotalIncomingAmount(@Param("accountId") Long accountId);

    /**
     * Compte le nombre de transactions d'un compte
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId")
    long countByAccountId(@Param("accountId") Long accountId);

    /**
     * Compte les transactions par type pour un compte
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) AND t.transactionType = :type")
    long countByAccountIdAndType(@Param("accountId") Long accountId, @Param("type") TransactionType type);

    /**
     * Trouve les dernières transactions d'un compte
     */
    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.transactionDate DESC")
    Page<Transaction> findRecentTransactionsByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    /**
     * Trouve les transactions par type pour un compte
     */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
           "AND t.transactionType = :type ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountIdAndType(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type);

    /**
     * Trouve les transactions complétées d'un compte
     */
    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId) " +
           "AND t.status = 'COMPLETED' ORDER BY t.transactionDate DESC")
    List<Transaction> findCompletedTransactionsByAccountId(@Param("accountId") Long accountId);

    /**
     * Calcule la somme des transactions par type dans une période
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type " +
           "AND t.status = 'COMPLETED' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve les transactions d'un compte source avec statut
     */
    List<Transaction> findBySourceAccountIdAndStatus(Long accountId, TransactionStatus status);

    /**
     * Trouve les transactions d'un compte destination avec statut
     */
    List<Transaction> findByDestinationAccountIdAndStatus(Long accountId, TransactionStatus status);

    /**
     * Recherche de transactions par description
     */
    @Query("SELECT t FROM Transaction t WHERE LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY t.transactionDate DESC")
    List<Transaction> searchByDescription(@Param("keyword") String keyword);

    /**
     * Statistiques : Nombre de transactions par jour
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE DATE(t.transactionDate) = DATE(:date)")
    long countTransactionsByDate(@Param("date") LocalDateTime date);

    /**
     * Trouve les transactions récentes (dernières 24h)
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :since ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("since") LocalDateTime since);

    /**
     * Compte le total des transactions complétées
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'COMPLETED'")
    long countCompletedTransactions();

    /**
     * Calcule le montant total de toutes les transactions complétées
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = 'COMPLETED'")
    BigDecimal getTotalCompletedAmount();
}
