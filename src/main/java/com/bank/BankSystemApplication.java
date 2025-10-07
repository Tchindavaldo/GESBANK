package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Classe principale de l'application Bank Management System
 *
 * Cette application fournit une API REST complète pour la gestion bancaire avec :
 * - Authentification JWT
 * - Gestion des comptes bancaires
 * - Transactions (dépôts, retraits, virements)
 * - Historique des transactions
 * - Documentation Swagger/OpenAPI
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class BankSystemApplication {

    /**
     * Point d'entrée de l'application
     *
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(BankSystemApplication.class, args);

        System.out.println("\n" +
                "╔══════════════════════════════════════════════════════════════════╗\n" +
                "║                                                                  ║\n" +
                "║     🏦  BANK MANAGEMENT SYSTEM - API REST                       ║\n" +
                "║                                                                  ║\n" +
                "║     ✅  Application démarrée avec succès !                       ║\n" +
                "║                                                                  ║\n" +
                "║     📚  Documentation Swagger UI :                               ║\n" +
                "║         http://localhost:8080/swagger-ui.html                    ║\n" +
                "║                                                                  ║\n" +
                "║     📖  API Docs (JSON) :                                        ║\n" +
                "║         http://localhost:8080/api-docs                           ║\n" +
                "║                                                                  ║\n" +
                "║     🔐  Endpoints Publics :                                      ║\n" +
                "║         POST /api/auth/register  - Créer un compte              ║\n" +
                "║         POST /api/auth/login     - Se connecter                 ║\n" +
                "║                                                                  ║\n" +
                "║     🔒  Endpoints Protégés (JWT requis) :                        ║\n" +
                "║         /api/accounts/**         - Gestion des comptes          ║\n" +
                "║         /api/transactions/**     - Gestion des transactions     ║\n" +
                "║                                                                  ║\n" +
                "║     💾  Base de données : PostgreSQL                             ║\n" +
                "║     🔧  Version : 1.0.0                                          ║\n" +
                "║                                                                  ║\n" +
                "╚══════════════════════════════════════════════════════════════════╝\n");
    }
}
