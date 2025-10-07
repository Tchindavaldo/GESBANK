package com.bank.controller;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour l'authentification des utilisateurs
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "APIs pour l'enregistrement et la connexion des utilisateurs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Enregistre un nouvel utilisateur
     */
    @PostMapping("/register")
    @Operation(
            summary = "Enregistrer un nouvel utilisateur",
            description = "Crée un nouveau compte utilisateur dans le système et retourne un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Utilisateur enregistré avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Données invalides",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "L'utilisateur existe déjà",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        logger.info("Requête d'enregistrement reçue pour l'utilisateur: {}", registerRequest.getUsername());

        AuthResponse authResponse = authService.register(registerRequest);

        logger.info("Utilisateur enregistré avec succès: {}", registerRequest.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Authentifie un utilisateur et retourne un token JWT
     */
    @PostMapping("/login")
    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur avec ses identifiants et retourne un token JWT"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion réussie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Identifiants invalides",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Requête de connexion reçue pour: {}", loginRequest.getUsernameOrEmail());

        AuthResponse authResponse = authService.login(loginRequest);

        logger.info("Connexion réussie pour: {}", loginRequest.getUsernameOrEmail());

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Rafraîchit le token JWT
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Rafraîchir le token",
            description = "Génère un nouveau token d'accès à partir d'un refresh token valide"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token rafraîchi avec succès",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalide ou expiré",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        logger.info("Requête de rafraîchissement de token reçue");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.warn("Refresh token manquant dans la requête");
            return ResponseEntity.badRequest().build();
        }

        AuthResponse authResponse = authService.refreshToken(refreshToken);

        logger.info("Token rafraîchi avec succès");

        return ResponseEntity.ok(authResponse);
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    @PostMapping("/logout")
    @Operation(
            summary = "Déconnexion utilisateur",
            description = "Déconnecte l'utilisateur actuel (invalide le token côté client)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Déconnexion réussie",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, String>> logout() {
        String username = authService.getCurrentUsername();

        logger.info("Requête de déconnexion reçue pour: {}", username);

        authService.logout(username);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Déconnexion réussie");
        response.put("username", username);

        logger.info("Utilisateur déconnecté: {}", username);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtient les informations de l'utilisateur actuellement connecté
     */
    @GetMapping("/me")
    @Operation(
            summary = "Obtenir l'utilisateur actuel",
            description = "Retourne les informations de l'utilisateur actuellement authentifié"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Informations utilisateur récupérées",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Non authentifié",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        var currentUser = authService.getCurrentUser();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", currentUser.getId());
        userInfo.put("username", currentUser.getUsername());
        userInfo.put("email", currentUser.getEmail());
        userInfo.put("firstName", currentUser.getFirstName());
        userInfo.put("lastName", currentUser.getLastName());
        userInfo.put("fullName", currentUser.getFullName());
        userInfo.put("phoneNumber", currentUser.getPhoneNumber());
        userInfo.put("enabled", currentUser.getEnabled());
        userInfo.put("roles", currentUser.getRoles());
        userInfo.put("createdAt", currentUser.getCreatedAt());
        userInfo.put("lastLogin", currentUser.getLastLogin());

        return ResponseEntity.ok(userInfo);
    }

    /**
     * Vérifie l'état de l'authentification
     */
    @GetMapping("/status")
    @Operation(
            summary = "Vérifier le statut d'authentification",
            description = "Vérifie si l'utilisateur est actuellement authentifié"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statut d'authentification",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Map<String, Object>> authStatus() {
        boolean isAuthenticated = authService.isAuthenticated();

        Map<String, Object> status = new HashMap<>();
        status.put("authenticated", isAuthenticated);

        if (isAuthenticated) {
            String username = authService.getCurrentUsername();
            status.put("username", username);
        }

        return ResponseEntity.ok(status);
    }
}
