package com.bank.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshExpirationMs;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Génère un token JWT à partir de l'authentification
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return generateTokenFromUsername(userPrincipal.getUsername(), jwtExpirationMs);
    }

    /**
     * Génère un token JWT à partir du nom d'utilisateur
     */
    public String generateTokenFromUsername(String username, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, expirationMs);
    }

    /**
     * Génère un token JWT avec des claims personnalisés
     */
    public String generateTokenWithClaims(String username, Map<String, Object> extraClaims, long expirationMs) {
        return createToken(extraClaims, username, expirationMs);
    }

    /**
     * Génère un refresh token
     */
    public String generateRefreshToken(String username) {
        return generateTokenFromUsername(username, jwtRefreshExpirationMs);
    }

    /**
     * Crée un token JWT
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Génère un token avec les rôles de l'utilisateur
     */
    public String generateTokenWithRoles(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("userId", userPrincipal.getId());
        claims.put("email", userPrincipal.getEmail());

        return createToken(claims, userPrincipal.getUsername(), jwtExpirationMs);
    }

    /**
     * Extrait le nom d'utilisateur du token JWT
     */
    public String getUsernameFromJwtToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrait l'ID utilisateur du token JWT
     */
    public Long getUserIdFromJwtToken(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * Extrait l'email du token JWT
     */
    public String getEmailFromJwtToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    /**
     * Extrait les claims du token JWT
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtient la date d'expiration du token
     */
    public Date getExpirationDateFromJwtToken(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Vérifie si le token est expiré
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromJwtToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Valide le token JWT
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException e) {
            logger.error("Signature JWT invalide: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Token JWT invalide: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expiré: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT non supporté: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string est vide: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrait le token JWT de l'en-tête Authorization
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Obtient la clé de signature
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Obtient le temps d'expiration du token en millisecondes
     */
    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Obtient le temps d'expiration du refresh token en millisecondes
     */
    public long getRefreshExpirationMs() {
        return jwtRefreshExpirationMs;
    }

    /**
     * Rafraîchit le token JWT
     */
    public String refreshToken(String token) {
        if (validateJwtToken(token) && !isTokenExpired(token)) {
            String username = getUsernameFromJwtToken(token);
            return generateTokenFromUsername(username, jwtExpirationMs);
        }
        throw new IllegalArgumentException("Token invalide ou expiré");
    }

    /**
     * Vérifie si le token peut être rafraîchi
     */
    public boolean canTokenBeRefreshed(String token) {
        return !isTokenExpired(token);
    }
}
