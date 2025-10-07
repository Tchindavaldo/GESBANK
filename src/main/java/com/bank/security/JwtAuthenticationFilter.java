package com.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT qui intercepte chaque requête HTTP
 * Extrait et valide le token JWT, puis configure l'authentification Spring Security
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Filtre les requêtes entrantes pour extraire et valider le token JWT
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extraire le token JWT de la requête
            String jwt = jwtUtils.getJwtFromRequest(request);

            // Valider et traiter le token s'il existe
            if (StringUtils.hasText(jwt) && jwtUtils.validateJwtToken(jwt)) {
                // Extraire le nom d'utilisateur du token
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                logger.debug("Token JWT valide pour l'utilisateur: {}", username);

                // Charger les détails de l'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Vérifier que l'utilisateur est actif et non verrouillé
                if (userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                    // Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Ajouter les détails de la requête à l'authentification
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Définir l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authentification configurée pour l'utilisateur: {} avec les rôles: {}",
                            username, userDetails.getAuthorities());
                } else {
                    logger.warn("Utilisateur désactivé ou verrouillé: {}", username);
                    SecurityContextHolder.clearContext();
                }
            } else if (StringUtils.hasText(jwt)) {
                logger.warn("Token JWT invalide ou expiré");
            }

        } catch (Exception e) {
            logger.error("Impossible de définir l'authentification utilisateur: {}", e.getMessage());
            // Ne pas bloquer la requête, laisser Spring Security gérer l'absence d'authentification
            SecurityContextHolder.clearContext();
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Détermine si le filtre doit être appliqué à la requête
     * Par défaut, appliqué à toutes les requêtes
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Optionnel: exclure certains chemins du filtrage
        String path = request.getRequestURI();

        // Ne pas filtrer les endpoints publics (Swagger, actuator, etc.)
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/actuator");
    }
}
