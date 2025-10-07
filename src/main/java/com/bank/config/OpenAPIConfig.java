package com.bank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI/Swagger
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Management System API",
                version = "1.0.0",
                description = """
                        API REST complète pour la gestion d'un système bancaire.

                        ## Fonctionnalités principales :
                        * **Authentification** : Enregistrement, connexion et gestion JWT
                        * **Comptes bancaires** : Création et gestion de comptes
                        * **Transactions** : Dépôts, retraits et virements
                        * **Historique** : Consultation de l'historique des transactions

                        ## Authentification :
                        Cette API utilise JWT (JSON Web Tokens) pour l'authentification.
                        1. Enregistrez-vous ou connectez-vous via `/api/auth/register` ou `/api/auth/login`
                        2. Utilisez le token reçu dans l'en-tête Authorization : `Bearer {token}`
                        3. Cliquez sur le bouton "Authorize" ci-dessus pour configurer votre token

                        ## Sécurité :
                        * Toutes les routes sauf `/api/auth/**` nécessitent une authentification
                        * Les tokens JWT expirent après 24 heures
                        * Les refresh tokens sont valables 7 jours
                        """,
                contact = @Contact(
                        name = "Bank System Support",
                        email = "support@banksystem.com",
                        url = "https://banksystem.com/support"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Serveur de développement local"
                ),
                @Server(
                        url = "https://api.banksystem.com",
                        description = "Serveur de production"
                ),
                @Server(
                        url = "https://staging-api.banksystem.com",
                        description = "Serveur de staging"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Authentification JWT - Utilisez le token obtenu lors de la connexion",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfig {

    /**
     * Configuration personnalisée de l'OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        // Réponses d'erreur communes
                        .addResponses("400", new ApiResponse()
                                .description("Requête invalide - Les données fournies sont incorrectes")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("validation-error", new Example()
                                                        .summary("Erreur de validation")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 400,
                                                                  "error": "Bad Request",
                                                                  "message": "Les données fournies sont invalides",
                                                                  "path": "/api/accounts",
                                                                  "errorCode": "VALIDATION_ERROR",
                                                                  "validationErrors": {
                                                                    "amount": "Le montant doit être positif"
                                                                  }
                                                                }
                                                                """)
                                                )
                                                .addExamples("insufficient-funds", new Example()
                                                        .summary("Solde insuffisant")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 400,
                                                                  "error": "Bad Request",
                                                                  "message": "Solde insuffisant",
                                                                  "path": "/api/transactions/withdraw",
                                                                  "errorCode": "INSUFFICIENT_FUNDS"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                        .addResponses("401", new ApiResponse()
                                .description("Non authentifié - Token manquant, invalide ou expiré")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("unauthorized", new Example()
                                                        .summary("Non authentifié")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 401,
                                                                  "error": "Unauthorized",
                                                                  "message": "Authentification requise pour accéder à cette ressource",
                                                                  "path": "/api/accounts",
                                                                  "errorCode": "AUTHENTICATION_FAILED"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                        .addResponses("403", new ApiResponse()
                                .description("Accès interdit - Permissions insuffisantes")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("forbidden", new Example()
                                                        .summary("Accès refusé")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 403,
                                                                  "error": "Forbidden",
                                                                  "message": "Vous n'avez pas les permissions nécessaires",
                                                                  "path": "/api/accounts/123",
                                                                  "errorCode": "ACCESS_DENIED"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                        .addResponses("404", new ApiResponse()
                                .description("Ressource non trouvée")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("not-found", new Example()
                                                        .summary("Ressource non trouvée")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 404,
                                                                  "error": "Not Found",
                                                                  "message": "Compte non trouvé avec l'ID: 123",
                                                                  "path": "/api/accounts/123",
                                                                  "errorCode": "ACCOUNT_NOT_FOUND"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                        .addResponses("409", new ApiResponse()
                                .description("Conflit - La ressource existe déjà")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("conflict", new Example()
                                                        .summary("Conflit")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 409,
                                                                  "error": "Conflict",
                                                                  "message": "L'utilisateur existe déjà",
                                                                  "path": "/api/auth/register",
                                                                  "errorCode": "USER_ALREADY_EXISTS"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                        .addResponses("500", new ApiResponse()
                                .description("Erreur interne du serveur")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .addExamples("internal-error", new Example()
                                                        .summary("Erreur serveur")
                                                        .value("""
                                                                {
                                                                  "timestamp": "2024-01-15T10:30:00",
                                                                  "status": 500,
                                                                  "error": "Internal Server Error",
                                                                  "message": "Une erreur interne est survenue",
                                                                  "path": "/api/transactions",
                                                                  "errorCode": "INTERNAL_ERROR"
                                                                }
                                                                """)
                                                )
                                        )
                                )
                        )
                );
    }
}
