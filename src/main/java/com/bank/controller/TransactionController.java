package com.bank.controller;

import com.bank.dto.TransactionRequest;
import com.bank.dto.TransactionResponse;
import com.bank.model.TransactionType;
import com.bank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des transactions bancaires
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "APIs pour la gestion des transactions bancaires (dépôts, retraits, virements)")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    /**
     * Effectue un dépôt sur un compte
     */
    @PostMapping("/accounts/{accountId}/deposit")
    @Operation(
            summary = "Effectuer un dépôt",
            description = "Dépose un montant d'argent sur un compte bancaire spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Dépôt effectué avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Montant invalide ou données incorrectes",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> deposit(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Requête de dépôt de {} sur le compte ID: {}", request.getAmount(), accountId);

        TransactionResponse transaction = transactionService.deposit(accountId, request);

        logger.info("Dépôt effectué avec succès - Référence: {}", transaction.getReferenceNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Effectue un retrait d'un compte
     */
    @PostMapping("/accounts/{accountId}/withdraw")
    @Operation(
            summary = "Effectuer un retrait",
            description = "Retire un montant d'argent d'un compte bancaire spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Retrait effectué avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Montant invalide, solde insuffisant ou données incorrectes",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> withdraw(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Requête de retrait de {} du compte ID: {}", request.getAmount(), accountId);

        TransactionResponse transaction = transactionService.withdraw(accountId, request);

        logger.info("Retrait effectué avec succès - Référence: {}", transaction.getReferenceNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Effectue un virement entre deux comptes
     */
    @PostMapping("/accounts/{accountId}/transfer")
    @Operation(
            summary = "Effectuer un virement",
            description = "Transfère un montant d'argent d'un compte vers un autre compte"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Virement effectué avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Montant invalide, solde insuffisant, devises incompatibles ou données incorrectes",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte source ou destination non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte source",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> transfer(
            @Parameter(description = "ID du compte source", required = true)
            @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {

        logger.info("Requête de virement de {} du compte ID: {} vers le compte: {}",
                request.getAmount(), accountId, request.getDestinationAccountNumber());

        TransactionResponse transaction = transactionService.transfer(accountId, request);

        logger.info("Virement effectué avec succès - Référence: {}", transaction.getReferenceNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    /**
     * Obtient l'historique des transactions d'un compte
     */
    @GetMapping("/accounts/{accountId}/history")
    @Operation(
            summary = "Obtenir l'historique des transactions d'un compte",
            description = "Récupère toutes les transactions (entrantes et sortantes) d'un compte spécifique"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historique des transactions récupéré",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactionHistory(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId) {

        logger.debug("Requête pour obtenir l'historique des transactions du compte ID: {}", accountId);

        List<TransactionResponse> transactions = transactionService.getAccountTransactionHistory(accountId);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient l'historique des transactions d'un compte avec pagination
     */
    @GetMapping("/accounts/{accountId}/history/paginated")
    @Operation(
            summary = "Obtenir l'historique paginé des transactions d'un compte",
            description = "Récupère les transactions d'un compte avec pagination et tri"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Page d'historique des transactions récupérée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactionHistoryPaginated(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Champ de tri")
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Direction du tri (ASC ou DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.debug("Requête pour obtenir l'historique paginé du compte ID: {} - Page: {}, Size: {}",
                accountId, page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionResponse> transactions = transactionService.getAccountTransactionHistory(
                accountId, pageable);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient l'historique des transactions de l'utilisateur connecté
     */
    @GetMapping("/history")
    @Operation(
            summary = "Obtenir l'historique des transactions de l'utilisateur",
            description = "Récupère toutes les transactions de tous les comptes de l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Historique des transactions récupéré",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getCurrentUserTransactionHistory() {

        logger.debug("Requête pour obtenir l'historique des transactions de l'utilisateur connecté");

        List<TransactionResponse> transactions = transactionService.getCurrentUserTransactionHistory();

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient l'historique des transactions de l'utilisateur avec pagination
     */
    @GetMapping("/history/paginated")
    @Operation(
            summary = "Obtenir l'historique paginé des transactions de l'utilisateur",
            description = "Récupère les transactions de l'utilisateur avec pagination et tri"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Page d'historique des transactions récupérée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<TransactionResponse>> getCurrentUserTransactionHistoryPaginated(
            @Parameter(description = "Numéro de page (commence à 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Champ de tri")
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Direction du tri (ASC ou DESC)")
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.debug("Requête pour obtenir l'historique paginé de l'utilisateur - Page: {}, Size: {}",
                page, size);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<TransactionResponse> transactions = transactionService.getCurrentUserTransactionHistory(pageable);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient une transaction par son numéro de référence
     */
    @GetMapping("/reference/{referenceNumber}")
    @Operation(
            summary = "Obtenir une transaction par référence",
            description = "Récupère les détails d'une transaction par son numéro de référence"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transaction trouvée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransactionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transaction non trouvée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé à cette transaction",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @Parameter(description = "Numéro de référence de la transaction", required = true)
            @PathVariable String referenceNumber) {

        logger.debug("Requête pour obtenir la transaction avec la référence: {}", referenceNumber);

        TransactionResponse transaction = transactionService.getTransactionByReference(referenceNumber);

        return ResponseEntity.ok(transaction);
    }

    /**
     * Obtient les transactions d'un compte par période
     */
    @GetMapping("/accounts/{accountId}/history/period")
    @Operation(
            summary = "Obtenir les transactions par période",
            description = "Récupère les transactions d'un compte dans une période donnée"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions récupérées",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactionsByDateRange(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "Date de début (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Date de fin (format: yyyy-MM-dd'T'HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.debug("Requête pour obtenir les transactions du compte ID: {} entre {} et {}",
                accountId, startDate, endDate);

        List<TransactionResponse> transactions = transactionService.getAccountTransactionsByDateRange(
                accountId, startDate, endDate);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient les transactions d'un compte par type
     */
    @GetMapping("/accounts/{accountId}/history/type/{transactionType}")
    @Operation(
            summary = "Obtenir les transactions par type",
            description = "Récupère les transactions d'un compte filtrées par type (DEPOSIT, WITHDRAWAL, TRANSFER)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions récupérées",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAccountTransactionsByType(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId,
            @Parameter(description = "Type de transaction", required = true)
            @PathVariable TransactionType transactionType) {

        logger.debug("Requête pour obtenir les transactions de type {} du compte ID: {}",
                transactionType, accountId);

        List<TransactionResponse> transactions = transactionService.getAccountTransactionsByType(
                accountId, transactionType);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtient les statistiques des transactions d'un compte
     */
    @GetMapping("/accounts/{accountId}/statistics")
    @Operation(
            summary = "Obtenir les statistiques des transactions",
            description = "Récupère les statistiques détaillées des transactions d'un compte"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistiques récupérées",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé au compte",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionService.TransactionStatistics> getAccountTransactionStatistics(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long accountId) {

        logger.debug("Requête pour obtenir les statistiques des transactions du compte ID: {}", accountId);

        TransactionService.TransactionStatistics statistics =
                transactionService.getAccountTransactionStatistics(accountId);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Endpoint de santé pour le service de transactions
     */
    @GetMapping("/health")
    @Operation(
            summary = "Vérifier la santé du service",
            description = "Endpoint de santé pour vérifier que le service de transactions est opérationnel"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service opérationnel",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Transaction Service");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
