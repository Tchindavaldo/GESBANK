package com.bank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Exception de base pour toutes les exceptions bancaires
 */
@Getter
public class BankingException extends RuntimeException {

    private final HttpStatus status;
    private final LocalDateTime timestamp;
    private final String errorCode;

    public BankingException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.timestamp = LocalDateTime.now();
        this.errorCode = "BANKING_ERROR";
    }

    public BankingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.errorCode = "BANKING_ERROR";
    }

    public BankingException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.timestamp = LocalDateTime.now();
        this.errorCode = "BANKING_ERROR";
    }

    public BankingException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.errorCode = errorCode;
    }

    /**
     * Exception pour solde insuffisant
     */
    public static class InsufficientFundsException extends BankingException {
        public InsufficientFundsException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS");
        }
    }

    /**
     * Exception pour compte non trouvé
     */
    public static class AccountNotFoundException extends BankingException {
        public AccountNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND");
        }
    }

    /**
     * Exception pour utilisateur non trouvé
     */
    public static class UserNotFoundException extends BankingException {
        public UserNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        }
    }

    /**
     * Exception pour transaction non trouvée
     */
    public static class TransactionNotFoundException extends BankingException {
        public TransactionNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND");
        }
    }

    /**
     * Exception pour transaction échouée
     */
    public static class TransactionFailedException extends BankingException {
        public TransactionFailedException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "TRANSACTION_FAILED");
        }

        public TransactionFailedException(String message, Throwable cause) {
            super(message, cause, HttpStatus.BAD_REQUEST, "TRANSACTION_FAILED");
        }
    }

    /**
     * Exception pour compte inactif
     */
    public static class AccountInactiveException extends BankingException {
        public AccountInactiveException(String message) {
            super(message, HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE");
        }
    }

    /**
     * Exception pour compte déjà existant
     */
    public static class AccountAlreadyExistsException extends BankingException {
        public AccountAlreadyExistsException(String message) {
            super(message, HttpStatus.CONFLICT, "ACCOUNT_ALREADY_EXISTS");
        }
    }

    /**
     * Exception pour utilisateur déjà existant
     */
    public static class UserAlreadyExistsException extends BankingException {
        public UserAlreadyExistsException(String message) {
            super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
        }
    }

    /**
     * Exception pour montant invalide
     */
    public static class InvalidAmountException extends BankingException {
        public InvalidAmountException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "INVALID_AMOUNT");
        }
    }

    /**
     * Exception pour opération non autorisée
     */
    public static class UnauthorizedOperationException extends BankingException {
        public UnauthorizedOperationException(String message) {
            super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_OPERATION");
        }
    }

    /**
     * Exception pour compte suspendu
     */
    public static class AccountSuspendedException extends BankingException {
        public AccountSuspendedException(String message) {
            super(message, HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED");
        }
    }

    /**
     * Exception pour limite de transaction dépassée
     */
    public static class TransactionLimitExceededException extends BankingException {
        public TransactionLimitExceededException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "TRANSACTION_LIMIT_EXCEEDED");
        }
    }

    /**
     * Exception pour devise invalide
     */
    public static class InvalidCurrencyException extends BankingException {
        public InvalidCurrencyException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "INVALID_CURRENCY");
        }
    }

    /**
     * Exception pour type de compte invalide
     */
    public static class InvalidAccountTypeException extends BankingException {
        public InvalidAccountTypeException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "INVALID_ACCOUNT_TYPE");
        }
    }
}
