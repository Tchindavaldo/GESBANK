package com.bank.service;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.exception.BankingException;
import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour gérer les transactions bancaires
 */
@Service
@Transactional
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final SecureRandom random = new SecureRandom();
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal MIN_TRANSACTION_AMOUNT = new BigDecimal("0.01");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    /**
     * Effectue un dépôt sur un compte
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse deposit(Long accountId, TransactionRequest request) {
        logger.info("Dépôt de {} sur le compte ID: {}", request.getAmount(), accountId);

        // Valider le montant
        validateAmount(request.getAmount());

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Valider que le compte est actif
        accountService.validateAccountIsActive(account);

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Dépôt sur le compte")
                .status(TransactionStatus.PENDING)
                .destinationAccount(account)
                .destinationBalanceBefore(account.getBalance())
                .referenceNumber(generateTransactionReference())
                .build();

        try {
            // Effectuer le dépôt
            account.deposit(request.getAmount());
            transaction.setDestinationBalanceAfter(account.getBalance());

            // Sauvegarder les changements
            accountRepository.save(account);
            transaction.complete();
            Transaction savedTransaction = transactionRepository.save(transaction);

            logger.info("Dépôt effectué avec succès. Référence: {}", savedTransaction.getReferenceNumber());

            return TransactionResponse.fromTransaction(savedTransaction);

        } catch (Exception e) {
            logger.error("Erreur lors du dépôt: {}", e.getMessage());
            transaction.fail(e.getMessage());
            transactionRepository.save(transaction);
            throw new BankingException.TransactionFailedException(
                    "Échec du dépôt: " + e.getMessage(), e
            );
        }
    }

    /**
     * Effectue un retrait d'un compte
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse withdraw(Long accountId, TransactionRequest request) {
        logger.info("Retrait de {} du compte ID: {}", request.getAmount(), accountId);

        // Valider le montant
        validateAmount(request.getAmount());

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Valider que le compte est actif
        accountService.validateAccountIsActive(account);

        // Vérifier le solde suffisant
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException.InsufficientFundsException(
                    "Solde insuffisant. Solde disponible: " + account.getBalance()
            );
        }

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .currency(account.getCurrency())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Retrait du compte")
                .status(TransactionStatus.PENDING)
                .sourceAccount(account)
                .sourceBalanceBefore(account.getBalance())
                .referenceNumber(generateTransactionReference())
                .build();

        try {
            // Effectuer le retrait
            account.withdraw(request.getAmount());
            transaction.setSourceBalanceAfter(account.getBalance());

            // Sauvegarder les changements
            accountRepository.save(account);
            transaction.complete();
            Transaction savedTransaction = transactionRepository.save(transaction);

            logger.info("Retrait effectué avec succès. Référence: {}", savedTransaction.getReferenceNumber());

            return TransactionResponse.fromTransaction(savedTransaction);

        } catch (Exception e) {
            logger.error("Erreur lors du retrait: {}", e.getMessage());
            transaction.fail(e.getMessage());
            transactionRepository.save(transaction);
            throw new BankingException.TransactionFailedException(
                    "Échec du retrait: " + e.getMessage(), e
            );
        }
    }

    /**
     * Effectue un virement entre deux comptes
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse transfer(Long sourceAccountId, TransactionRequest request) {
        logger.info("Virement de {} du compte ID: {} vers le compte: {}",
                request.getAmount(), sourceAccountId, request.getDestinationAccountNumber());

        // Valider le montant
        validateAmount(request.getAmount());

        // Valider que le numéro de compte destination est fourni
        if (request.getDestinationAccountNumber() == null ||
                request.getDestinationAccountNumber().trim().isEmpty()) {
            throw new BankingException.InvalidAmountException(
                    "Le numéro de compte destination est obligatoire"
            );
        }

        // Récupérer les comptes
        Account sourceAccount = accountService.getAccountEntityById(sourceAccountId);
        Account destinationAccount = accountService.getAccountEntityByNumber(
                request.getDestinationAccountNumber()
        );

        // Valider que les comptes sont différents
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BankingException.InvalidAmountException(
                    "Le compte source et le compte destination doivent être différents"
            );
        }

        // Valider que l'utilisateur connecté possède le compte source
        validateAccountOwnership(sourceAccount);

        // Valider que les deux comptes sont actifs
        accountService.validateAccountIsActive(sourceAccount);
        accountService.validateAccountIsActive(destinationAccount);

        // Vérifier que les devises correspondent
        if (!sourceAccount.getCurrency().equals(destinationAccount.getCurrency())) {
            throw new BankingException.InvalidCurrencyException(
                    "Les devises des comptes ne correspondent pas"
            );
        }

        // Vérifier le solde suffisant
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BankingException.InsufficientFundsException(
                    "Solde insuffisant. Solde disponible: " + sourceAccount.getBalance()
            );
        }

        // Créer la transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .currency(sourceAccount.getCurrency())
                .description(request.getDescription() != null ?
                        request.getDescription() :
                        "Virement vers " + destinationAccount.getAccountNumber())
                .status(TransactionStatus.PENDING)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .sourceBalanceBefore(sourceAccount.getBalance())
                .destinationBalanceBefore(destinationAccount.getBalance())
                .referenceNumber(generateTransactionReference())
                .build();

        try {
            // Effectuer le virement
            sourceAccount.withdraw(request.getAmount());
            destinationAccount.deposit(request.getAmount());

            transaction.setSourceBalanceAfter(sourceAccount.getBalance());
            transaction.setDestinationBalanceAfter(destinationAccount.getBalance());

            // Sauvegarder les changements
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);
            transaction.complete();
            Transaction savedTransaction = transactionRepository.save(transaction);

            logger.info("Virement effectué avec succès. Référence: {}",
                    savedTransaction.getReferenceNumber());

            return TransactionResponse.fromTransaction(savedTransaction);

        } catch (Exception e) {
            logger.error("Erreur lors du virement: {}", e.getMessage());
            transaction.fail(e.getMessage());
            transactionRepository.save(transaction);
            throw new BankingException.TransactionFailedException(
                    "Échec du virement: " + e.getMessage(), e
            );
        }
    }

    /**
     * Obtient l'historique des transactions d'un compte
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactionHistory(Long accountId) {
        logger.debug("Récupération de l'historique des transactions pour le compte ID: {}", accountId);

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Récupérer les transactions
        List<Transaction> transactions = transactionRepository.findAllByAccountId(accountId);

        return transactions.stream()
                .map(TransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Obtient l'historique des transactions d'un compte avec pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactionHistory(Long accountId, Pageable pageable) {
        logger.debug("Récupération de l'historique paginé des transactions pour le compte ID: {}", accountId);

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Récupérer les transactions avec pagination
        Page<Transaction> transactions = transactionRepository.findAllByAccountId(accountId, pageable);

        return transactions.map(TransactionResponse::fromTransaction);
    }

    /**
     * Obtient l'historique des transactions de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getCurrentUserTransactionHistory() {
        User currentUser = authService.getCurrentUser();
        logger.debug("Récupération de l'historique des transactions pour l'utilisateur: {}",
                currentUser.getUsername());

        List<Transaction> transactions = transactionRepository.findAllByUserId(currentUser.getId());

        return transactions.stream()
                .map(TransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Obtient l'historique des transactions de l'utilisateur connecté avec pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getCurrentUserTransactionHistory(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        logger.debug("Récupération de l'historique paginé des transactions pour l'utilisateur: {}",
                currentUser.getUsername());

        Page<Transaction> transactions = transactionRepository.findAllByUserId(currentUser.getId(), pageable);

        return transactions.map(TransactionResponse::fromTransaction);
    }

    /**
     * Obtient une transaction par son numéro de référence
     */
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(String referenceNumber) {
        logger.debug("Récupération de la transaction avec la référence: {}", referenceNumber);

        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new BankingException.TransactionNotFoundException(
                        "Transaction non trouvée avec la référence: " + referenceNumber
                ));

        // Valider que l'utilisateur connecté a accès à cette transaction
        validateTransactionAccess(transaction);

        return TransactionResponse.fromTransaction(transaction);
    }

    /**
     * Obtient les transactions d'un compte dans une période donnée
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactionsByDateRange(
            Long accountId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        logger.debug("Récupération des transactions du compte ID: {} entre {} et {}",
                accountId, startDate, endDate);

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Récupérer les transactions
        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(
                accountId, startDate, endDate
        );

        return transactions.stream()
                .map(TransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Obtient les transactions par type pour un compte
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactionsByType(
            Long accountId,
            TransactionType transactionType) {

        logger.debug("Récupération des transactions de type {} pour le compte ID: {}",
                transactionType, accountId);

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Récupérer les transactions
        List<Transaction> transactions = transactionRepository.findByAccountIdAndType(
                accountId, transactionType
        );

        return transactions.stream()
                .map(TransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    /**
     * Obtient les statistiques des transactions pour un compte
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getAccountTransactionStatistics(Long accountId) {
        logger.debug("Récupération des statistiques des transactions pour le compte ID: {}", accountId);

        // Récupérer le compte
        Account account = accountService.getAccountEntityById(accountId);

        // Valider que l'utilisateur connecté possède ce compte
        validateAccountOwnership(account);

        // Calculer les statistiques
        BigDecimal totalIncoming = transactionRepository.getTotalIncomingAmount(accountId);
        BigDecimal totalOutgoing = transactionRepository.getTotalOutgoingAmount(accountId);
        long transactionCount = transactionRepository.countByAccountId(accountId);

        long depositCount = transactionRepository.countByAccountIdAndType(accountId, TransactionType.DEPOSIT);
        long withdrawalCount = transactionRepository.countByAccountIdAndType(accountId, TransactionType.WITHDRAWAL);
        long transferCount = transactionRepository.countByAccountIdAndType(accountId, TransactionType.TRANSFER);

        return new TransactionStatistics(
                transactionCount,
                depositCount,
                withdrawalCount,
                transferCount,
                totalIncoming,
                totalOutgoing,
                account.getBalance(),
                LocalDateTime.now()
        );
    }

    /**
     * Valide que le montant est correct
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BankingException.InvalidAmountException("Le montant est obligatoire");
        }

        if (amount.compareTo(MIN_TRANSACTION_AMOUNT) < 0) {
            throw new BankingException.InvalidAmountException(
                    "Le montant doit être au moins " + MIN_TRANSACTION_AMOUNT
            );
        }

        if (amount.compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            throw new BankingException.TransactionLimitExceededException(
                    "Le montant maximum autorisé est " + MAX_TRANSACTION_AMOUNT
            );
        }

        if (amount.scale() > 2) {
            throw new BankingException.InvalidAmountException(
                    "Le montant ne peut pas avoir plus de 2 décimales"
            );
        }
    }

    /**
     * Valide que l'utilisateur connecté possède le compte
     */
    private void validateAccountOwnership(Account account) {
        User currentUser = authService.getCurrentUser();

        if (!account.getUser().getId().equals(currentUser.getId())) {
            logger.warn("Tentative d'accès non autorisé au compte {} par l'utilisateur {}",
                    account.getAccountNumber(), currentUser.getUsername());
            throw new BankingException.UnauthorizedOperationException(
                    "Vous n'avez pas l'autorisation d'effectuer cette opération sur ce compte"
            );
        }
    }

    /**
     * Valide que l'utilisateur connecté a accès à la transaction
     */
    private void validateTransactionAccess(Transaction transaction) {
        User currentUser = authService.getCurrentUser();

        boolean hasAccess = false;

        if (transaction.getSourceAccount() != null &&
                transaction.getSourceAccount().getUser().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }

        if (transaction.getDestinationAccount() != null &&
                transaction.getDestinationAccount().getUser().getId().equals(currentUser.getId())) {
            hasAccess = true;
        }

        if (!hasAccess) {
            logger.warn("Tentative d'accès non autorisé à la transaction {} par l'utilisateur {}",
                    transaction.getReferenceNumber(), currentUser.getUsername());
            throw new BankingException.UnauthorizedOperationException(
                    "Vous n'avez pas l'autorisation d'accéder à cette transaction"
            );
        }
    }

    /**
     * Génère un numéro de référence unique pour la transaction
     */
    private String generateTransactionReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + timestamp + "-" + uuid;
    }

    /**
     * Classe interne pour les statistiques des transactions
     */
    public static class TransactionStatistics {
        private final long totalTransactions;
        private final long depositCount;
        private final long withdrawalCount;
        private final long transferCount;
        private final BigDecimal totalIncoming;
        private final BigDecimal totalOutgoing;
        private final BigDecimal currentBalance;
        private final LocalDateTime timestamp;

        public TransactionStatistics(long totalTransactions, long depositCount,
                                    long withdrawalCount, long transferCount,
                                    BigDecimal totalIncoming, BigDecimal totalOutgoing,
                                    BigDecimal currentBalance, LocalDateTime timestamp) {
            this.totalTransactions = totalTransactions;
            this.depositCount = depositCount;
            this.withdrawalCount = withdrawalCount;
            this.transferCount = transferCount;
            this.totalIncoming = totalIncoming;
            this.totalOutgoing = totalOutgoing;
            this.currentBalance = currentBalance;
            this.timestamp = timestamp;
        }

        public long getTotalTransactions() {
            return totalTransactions;
        }

        public long getDepositCount() {
            return depositCount;
        }

        public long getWithdrawalCount() {
            return withdrawalCount;
        }

        public long getTransferCount() {
            return transferCount;
        }

        public BigDecimal getTotalIncoming() {
            return totalIncoming;
        }

        public BigDecimal getTotalOutgoing() {
            return totalOutgoing;
        }

        public BigDecimal getCurrentBalance() {
            return currentBalance;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public BigDecimal getNetChange() {
            return totalIncoming.subtract(totalOutgoing);
        }
    }
}
