package com.bank.service;

import com.bank.dto.AccountResponse;
import com.bank.dto.CreateAccountRequest;
import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.model.AccountStatus;
import com.bank.model.User;
import com.bank.repository.AccountRepository;
import com.bank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer les comptes bancaires
 */
@Service
@Transactional
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final SecureRandom random = new SecureRandom();
    private static final String ACCOUNT_NUMBER_PREFIX = "FR76";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    /**
     * Crée un nouveau compte bancaire pour l'utilisateur connecté
     */
    public AccountResponse createAccount(CreateAccountRequest request) {
        logger.info("Création d'un nouveau compte de type: {}", request.getAccountType());

        // Récupérer l'utilisateur connecté
        User currentUser = authService.getCurrentUser();

        // Générer un numéro de compte unique
        String accountNumber = generateUniqueAccountNumber();

        // Créer le compte
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "EUR")
                .status(AccountStatus.ACTIVE)
                .user(currentUser)
                .build();

        // Valider le solde initial
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new BankingException.InvalidAmountException(
                    "Le solde initial ne peut pas être négatif"
            );
        }

        // Sauvegarder le compte
        Account savedAccount = accountRepository.save(account);
        logger.info("Compte créé avec succès: {} pour l'utilisateur: {}",
                savedAccount.getAccountNumber(), currentUser.getUsername());

        return AccountResponse.fromAccount(savedAccount);
    }

    /**
     * Obtient un compte par son ID
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long accountId) {
        logger.debug("Récupération du compte avec l'ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec l'ID: " + accountId
                ));

        // Vérifier que l'utilisateur connecté a accès à ce compte
        validateAccountAccess(account);

        return AccountResponse.fromAccount(account);
    }

    /**
     * Obtient un compte par son numéro
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        logger.debug("Récupération du compte avec le numéro: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec le numéro: " + accountNumber
                ));

        // Vérifier que l'utilisateur connecté a accès à ce compte
        validateAccountAccess(account);

        return AccountResponse.fromAccount(account);
    }

    /**
     * Obtient tous les comptes de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getCurrentUserAccounts() {
        User currentUser = authService.getCurrentUser();
        logger.debug("Récupération de tous les comptes de l'utilisateur: {}", currentUser.getUsername());

        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());

        return accounts.stream()
                .map(AccountResponse::fromAccount)
                .collect(Collectors.toList());
    }

    /**
     * Obtient tous les comptes actifs de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getCurrentUserActiveAccounts() {
        User currentUser = authService.getCurrentUser();
        logger.debug("Récupération des comptes actifs de l'utilisateur: {}", currentUser.getUsername());

        List<Account> accounts = accountRepository.findByUserIdAndStatus(
                currentUser.getId(),
                AccountStatus.ACTIVE
        );

        return accounts.stream()
                .map(AccountResponse::fromAccount)
                .collect(Collectors.toList());
    }

    /**
     * Obtient le solde total de tous les comptes de l'utilisateur connecté
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance() {
        User currentUser = authService.getCurrentUser();
        logger.debug("Calcul du solde total pour l'utilisateur: {}", currentUser.getUsername());

        return accountRepository.getTotalBalanceByUserId(
                currentUser.getId(),
                AccountStatus.ACTIVE
        );
    }

    /**
     * Désactive un compte
     */
    public AccountResponse deactivateAccount(Long accountId) {
        logger.info("Désactivation du compte avec l'ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec l'ID: " + accountId
                ));

        // Vérifier que l'utilisateur connecté a accès à ce compte
        validateAccountAccess(account);

        // Vérifier que le compte est actif
        if (!account.isActive()) {
            throw new BankingException.AccountInactiveException(
                    "Le compte est déjà inactif"
            );
        }

        // Vérifier que le solde est nul
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BankingException("Le compte doit avoir un solde nul pour être désactivé");
        }

        account.deactivate();
        Account updatedAccount = accountRepository.save(account);

        logger.info("Compte désactivé avec succès: {}", account.getAccountNumber());

        return AccountResponse.fromAccount(updatedAccount);
    }

    /**
     * Suspend un compte
     */
    public AccountResponse suspendAccount(Long accountId) {
        logger.info("Suspension du compte avec l'ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec l'ID: " + accountId
                ));

        // Vérifier que l'utilisateur connecté a accès à ce compte
        validateAccountAccess(account);

        account.suspend();
        Account updatedAccount = accountRepository.save(account);

        logger.info("Compte suspendu avec succès: {}", account.getAccountNumber());

        return AccountResponse.fromAccount(updatedAccount);
    }

    /**
     * Réactive un compte
     */
    public AccountResponse reactivateAccount(Long accountId) {
        logger.info("Réactivation du compte avec l'ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec l'ID: " + accountId
                ));

        // Vérifier que l'utilisateur connecté a accès à ce compte
        validateAccountAccess(account);

        // Vérifier que le compte n'est pas déjà actif
        if (account.isActive()) {
            throw new BankingException("Le compte est déjà actif");
        }

        account.activate();
        account.setClosedAt(null);
        Account updatedAccount = accountRepository.save(account);

        logger.info("Compte réactivé avec succès: {}", account.getAccountNumber());

        return AccountResponse.fromAccount(updatedAccount);
    }

    /**
     * Obtient le compte par ID (méthode interne)
     */
    protected Account getAccountEntityById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec l'ID: " + accountId
                ));
    }

    /**
     * Obtient le compte par numéro (méthode interne)
     */
    protected Account getAccountEntityByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BankingException.AccountNotFoundException(
                        "Compte non trouvé avec le numéro: " + accountNumber
                ));
    }

    /**
     * Valide que le compte est actif
     */
    protected void validateAccountIsActive(Account account) {
        if (!account.isActive()) {
            throw new BankingException.AccountInactiveException(
                    "Le compte " + account.getAccountNumber() + " n'est pas actif"
            );
        }

        if (account.getStatus() == AccountStatus.SUSPENDED) {
            throw new BankingException.AccountSuspendedException(
                    "Le compte " + account.getAccountNumber() + " est suspendu"
            );
        }
    }

    /**
     * Valide que l'utilisateur connecté a accès au compte
     */
    private void validateAccountAccess(Account account) {
        User currentUser = authService.getCurrentUser();

        if (!account.getUser().getId().equals(currentUser.getId())) {
            logger.warn("Tentative d'accès non autorisé au compte {} par l'utilisateur {}",
                    account.getAccountNumber(), currentUser.getUsername());
            throw new BankingException.UnauthorizedOperationException(
                    "Vous n'avez pas l'autorisation d'accéder à ce compte"
            );
        }
    }

    /**
     * Génère un numéro de compte unique
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            // Générer un numéro de compte au format: FR76 XXXX XXXX XXXX XXXX XX
            StringBuilder sb = new StringBuilder(ACCOUNT_NUMBER_PREFIX);

            // Générer 16 chiffres aléatoires
            for (int i = 0; i < 16; i++) {
                sb.append(random.nextInt(10));
            }

            accountNumber = sb.toString();
            attempts++;

            if (attempts >= maxAttempts) {
                logger.error("Impossible de générer un numéro de compte unique après {} tentatives", maxAttempts);
                throw new BankingException("Impossible de générer un numéro de compte unique");
            }

        } while (accountRepository.existsByAccountNumber(accountNumber));

        logger.debug("Numéro de compte généré: {}", accountNumber);
        return accountNumber;
    }

    /**
     * Obtient les statistiques des comptes de l'utilisateur
     */
    @Transactional(readOnly = true)
    public AccountStatistics getAccountStatistics() {
        User currentUser = authService.getCurrentUser();
        logger.debug("Récupération des statistiques des comptes pour: {}", currentUser.getUsername());

        long totalAccounts = accountRepository.countByUserId(currentUser.getId());
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(
                currentUser.getId(),
                AccountStatus.ACTIVE
        );

        List<Account> accounts = accountRepository.findByUserId(currentUser.getId());

        long activeAccounts = accounts.stream()
                .filter(Account::isActive)
                .count();

        return new AccountStatistics(
                totalAccounts,
                activeAccounts,
                totalBalance,
                LocalDateTime.now()
        );
    }

    /**
     * Classe interne pour les statistiques des comptes
     */
    public static class AccountStatistics {
        private final long totalAccounts;
        private final long activeAccounts;
        private final BigDecimal totalBalance;
        private final LocalDateTime timestamp;

        public AccountStatistics(long totalAccounts, long activeAccounts,
                                BigDecimal totalBalance, LocalDateTime timestamp) {
            this.totalAccounts = totalAccounts;
            this.activeAccounts = activeAccounts;
            this.totalBalance = totalBalance;
            this.timestamp = timestamp;
        }

        public long getTotalAccounts() {
            return totalAccounts;
        }

        public long getActiveAccounts() {
            return activeAccounts;
        }

        public BigDecimal getTotalBalance() {
            return totalBalance;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
