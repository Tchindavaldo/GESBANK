package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.AccountType;
import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Trouve un compte par son numéro de compte
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Vérifie si un numéro de compte existe déjà
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Trouve tous les comptes d'un utilisateur
     */
    List<Account> findByUser(User user);

    /**
     * Trouve tous les comptes d'un utilisateur par son ID
     */
    List<Account> findByUserId(Long userId);

    /**
     * Trouve tous les comptes actifs d'un utilisateur
     */
    List<Account> findByUserIdAndStatus(Long userId, AccountStatus status);

    /**
     * Trouve tous les comptes par type
     */
    List<Account> findByAccountType(AccountType accountType);

    /**
     * Trouve tous les comptes par statut
     */
    List<Account> findByStatus(AccountStatus status);

    /**
     * Trouve tous les comptes actifs
     */
    List<Account> findByStatusOrderByCreatedAtDesc(AccountStatus status);

    /**
     * Trouve les comptes avec un solde supérieur à un montant donné
     */
    List<Account> findByBalanceGreaterThan(BigDecimal amount);

    /**
     * Trouve les comptes avec un solde inférieur à un montant donné
     */
    List<Account> findByBalanceLessThan(BigDecimal amount);

    /**
     * Trouve un compte avec ses transactions
     */
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.outgoingTransactions " +
           "LEFT JOIN FETCH a.incomingTransactions WHERE a.id = :accountId")
    Optional<Account> findByIdWithTransactions(@Param("accountId") Long accountId);

    /**
     * Trouve un compte par numéro avec son utilisateur
     */
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.user WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithUser(@Param("accountNumber") String accountNumber);

    /**
     * Trouve tous les comptes d'un utilisateur avec le total des soldes
     */
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
    List<Account> findAllByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * Calcule le solde total de tous les comptes d'un utilisateur
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId AND a.status = :status")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId, @Param("status") AccountStatus status);

    /**
     * Compte le nombre de comptes par utilisateur
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * Trouve les comptes créés après une certaine date
     */
    List<Account> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Trouve les comptes fermés
     */
    List<Account> findByStatusAndClosedAtIsNotNull(AccountStatus status);

    /**
     * Recherche de comptes par numéro de compte (partiel)
     */
    @Query("SELECT a FROM Account a WHERE a.accountNumber LIKE CONCAT('%', :accountNumber, '%')")
    List<Account> searchByAccountNumber(@Param("accountNumber") String accountNumber);

    /**
     * Trouve les comptes actifs d'un utilisateur avec un solde minimum
     */
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE' AND a.balance >= :minBalance")
    List<Account> findActiveAccountsWithMinBalance(
            @Param("userId") Long userId,
            @Param("minBalance") BigDecimal minBalance);

    /**
     * Compte le nombre total de comptes actifs
     */
    @Query("SELECT COUNT(a) FROM Account a WHERE a.status = 'ACTIVE'")
    long countActiveAccounts();

    /**
     * Calcule le solde total de tous les comptes actifs
     */
    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.status = 'ACTIVE'")
    BigDecimal getTotalActiveBalance();

    /**
     * Trouve les comptes par type et statut
     */
    List<Account> findByAccountTypeAndStatus(AccountType accountType, AccountStatus status);
}
