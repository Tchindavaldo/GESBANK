package com.bank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Point d'entrée d'authentification JWT
 * Gère les erreurs d'authentification et renvoie une réponse JSON appropriée
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Méthode appelée lorsqu'un utilisateur non authentifié tente d'accéder à une ressource protégée
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        logger.error("Erreur d'authentification non autorisée: {}", authException.getMessage());
        logger.debug("Chemin de la requête: {}, Méthode: {}", request.getRequestURI(), request.getMethod());

        // Configurer la réponse HTTP
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");

        // Construire le corps de la réponse d'erreur
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", authException.getMessage() != null ?
                authException.getMessage() : "Authentification requise pour accéder à cette ressource");
        errorDetails.put("path", request.getRequestURI());

        // Ajouter des informations supplémentaires si disponibles
        Object errorAttribute = request.getAttribute("javax.servlet.error.message");
        if (errorAttribute != null) {
            errorDetails.put("details", errorAttribute.toString());
        }

        // Convertir la map en JSON et l'écrire dans la réponse
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorDetails);
    }
}
