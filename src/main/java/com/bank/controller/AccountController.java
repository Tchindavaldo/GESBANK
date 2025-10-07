package com.bank.controller;

import com.bank.dto.AccountResponse;
import com.bank.dto.CreateAccountRequest;
import com.bank.service.AccountService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des comptes bancaires
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Comptes Bancaires", description = "APIs pour la gestion des comptes bancaires")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    /**
     * Crée un nouveau compte bancaire
     */
    @PostMapping
    @Operation(
            summary = "Créer un nouveau compte",
            description = "Crée un nouveau compte bancaire pour l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Compte créé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        logger.info("Requête de création de compte reçue - Type: {}", request.getAccountType());

        AccountResponse accountResponse = accountService.createAccount(request);

        logger.info("Compte créé avec succès - Numéro: {}", accountResponse.getAccountNumber());

        return ResponseEntity.status(HttpStatus.CREATED).body(accountResponse);
    }

    /**
     * Obtient un compte par son ID
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtenir un compte par ID",
            description = "Récupère les détails d'un compte bancaire par son identifiant"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> getAccountById(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long id) {

        logger.debug("Requête pour obtenir le compte ID: {}", id);

        AccountResponse accountResponse = accountService.getAccountById(id);

        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Obtient un compte par son numéro
     */
    @GetMapping("/number/{accountNumber}")
    @Operation(
            summary = "Obtenir un compte par numéro",
            description = "Récupère les détails d'un compte bancaire par son numéro"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte trouvé",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @Parameter(description = "Numéro du compte", required = true)
            @PathVariable String accountNumber) {

        logger.debug("Requête pour obtenir le compte numéro: {}", accountNumber);

        AccountResponse accountResponse = accountService.getAccountByNumber(accountNumber);

        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Obtient tous les comptes de l'utilisateur connecté
     */
    @GetMapping
    @Operation(
            summary = "Obtenir tous les comptes de l'utilisateur",
            description = "Récupère la liste de tous les comptes bancaires de l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des comptes récupérée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<AccountResponse>> getCurrentUserAccounts() {

        logger.debug("Requête pour obtenir tous les comptes de l'utilisateur connecté");

        List<AccountResponse> accounts = accountService.getCurrentUserAccounts();

        return ResponseEntity.ok(accounts);
    }

    /**
     * Obtient tous les comptes actifs de l'utilisateur connecté
     */
    @GetMapping("/active")
    @Operation(
            summary = "Obtenir les comptes actifs",
            description = "Récupère la liste des comptes actifs de l'utilisateur connecté"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des comptes actifs récupérée",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<AccountResponse>> getCurrentUserActiveAccounts() {

        logger.debug("Requête pour obtenir les comptes actifs de l'utilisateur connecté");

        List<AccountResponse> accounts = accountService.getCurrentUserActiveAccounts();

        return ResponseEntity.ok(accounts);
    }

    /**
     * Obtient le solde total de tous les comptes de l'utilisateur
     */
    @GetMapping("/balance/total")
    @Operation(
            summary = "Obtenir le solde total",
            description = "Calcule et retourne le solde total de tous les comptes actifs de l'utilisateur"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Solde total calculé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTotalBalance() {

        logger.debug("Requête pour obtenir le solde total de l'utilisateur");

        BigDecimal totalBalance = accountService.getTotalBalance();

        Map<String, Object> response = new HashMap<>();
        response.put("totalBalance", totalBalance);
        response.put("currency", "EUR");

        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les statistiques des comptes de l'utilisateur
     */
    @GetMapping("/statistics")
    @Operation(
            summary = "Obtenir les statistiques des comptes",
            description = "Récupère les statistiques globales des comptes de l'utilisateur"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistiques récupérées",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountService.AccountStatistics> getAccountStatistics() {

        logger.debug("Requête pour obtenir les statistiques des comptes");

        AccountService.AccountStatistics statistics = accountService.getAccountStatistics();

        return ResponseEntity.ok(statistics);
    }

    /**
     * Désactive un compte
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(
            summary = "Désactiver un compte",
            description = "Désactive un compte bancaire (le solde doit être à zéro)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte désactivé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Le compte ne peut pas être désactivé (solde non nul)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long id) {

        logger.info("Requête de désactivation du compte ID: {}", id);

        AccountResponse accountResponse = accountService.deactivateAccount(id);

        logger.info("Compte désactivé avec succès - Numéro: {}", accountResponse.getAccountNumber());

        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Suspend un compte
     */
    @PatchMapping("/{id}/suspend")
    @Operation(
            summary = "Suspendre un compte",
            description = "Suspend temporairement un compte bancaire"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte suspendu avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> suspendAccount(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long id) {

        logger.info("Requête de suspension du compte ID: {}", id);

        AccountResponse accountResponse = accountService.suspendAccount(id);

        logger.info("Compte suspendu avec succès - Numéro: {}", accountResponse.getAccountNumber());

        return ResponseEntity.ok(accountResponse);
    }

    /**
     * Réactive un compte
     */
    @PatchMapping("/{id}/reactivate")
    @Operation(
            summary = "Réactiver un compte",
            description = "Réactive un compte bancaire suspendu ou inactif"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Compte réactivé avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Compte non trouvé",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Accès non autorisé",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponse> reactivateAccount(
            @Parameter(description = "ID du compte", required = true)
            @PathVariable Long id) {

        logger.info("Requête de réactivation du compte ID: {}", id);

        AccountResponse accountResponse = accountService.reactivateAccount(id);

        logger.info("Compte réactivé avec succès - Numéro: {}", accountResponse.getAccountNumber());

        return ResponseEntity.ok(accountResponse);
    }
}
