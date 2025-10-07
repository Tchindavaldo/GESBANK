package com.bank.service;

import com.bank.dto.AuthResponse;
import com.bank.dto.LoginRequest;
import com.bank.dto.RegisterRequest;
import com.bank.exception.BankingException;
import com.bank.model.Role;
import com.bank.model.User;
import com.bank.repository.UserRepository;
import com.bank.security.JwtUtils;
import com.bank.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service d'authentification pour gérer l'inscription et la connexion des utilisateurs
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Enregistre un nouvel utilisateur dans le système
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Tentative d'enregistrement d'un nouvel utilisateur: {}", registerRequest.getUsername());

        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Nom d'utilisateur déjà existant: {}", registerRequest.getUsername());
            throw new BankingException.UserAlreadyExistsException(
                    "Le nom d'utilisateur '" + registerRequest.getUsername() + "' est déjà utilisé"
            );
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Email déjà existant: {}", registerRequest.getEmail());
            throw new BankingException.UserAlreadyExistsException(
                    "L'email '" + registerRequest.getEmail() + "' est déjà utilisé"
            );
        }

        // Créer le nouvel utilisateur
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .enabled(true)
                .accountNonLocked(true)
                .roles(new HashSet<>())
                .build();

        // Ajouter le rôle par défaut
        user.addRole(Role.ROLE_USER);

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        logger.info("Nouvel utilisateur enregistré avec succès: {}", savedUser.getUsername());

        // Authentifier automatiquement l'utilisateur après l'inscription
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getUsername(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer les tokens JWT
        String accessToken = jwtUtils.generateTokenWithRoles(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(savedUser.getUsername());

        // Créer la réponse avec les informations utilisateur
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        AuthResponse.UserInfo userInfo = createUserInfo(userDetails);

        logger.info("Tokens JWT générés pour l'utilisateur: {}", savedUser.getUsername());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtUtils.getExpirationMs(),
                userInfo
        );
    }

    /**
     * Authentifie un utilisateur et génère les tokens JWT
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Tentative de connexion pour: {}", loginRequest.getUsernameOrEmail());

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Mettre à jour la date de dernière connexion
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new BankingException.UserNotFoundException(
                            "Utilisateur non trouvé: " + userDetails.getUsername()
                    ));

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Générer les tokens JWT
            String accessToken = jwtUtils.generateTokenWithRoles(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            // Créer la réponse avec les informations utilisateur
            AuthResponse.UserInfo userInfo = createUserInfo(userDetails);

            logger.info("Utilisateur connecté avec succès: {}", userDetails.getUsername());

            return AuthResponse.of(
                    accessToken,
                    refreshToken,
                    jwtUtils.getExpirationMs(),
                    userInfo
            );

        } catch (BadCredentialsException e) {
            logger.error("Échec de la connexion pour: {} - Identifiants incorrects", loginRequest.getUsernameOrEmail());
            throw new BadCredentialsException("Nom d'utilisateur ou mot de passe incorrect");
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion: {}", e.getMessage());
            throw new BankingException(
                    "Erreur lors de l'authentification: " + e.getMessage()
            );
        }
    }

    /**
     * Rafraîchit le token JWT
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Tentative de rafraîchissement du token");

        try {
            // Valider le refresh token
            if (!jwtUtils.validateJwtToken(refreshToken)) {
                throw new IllegalArgumentException("Refresh token invalide");
            }

            // Extraire le nom d'utilisateur du token
            String username = jwtUtils.getUsernameFromJwtToken(refreshToken);

            // Charger l'utilisateur
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BankingException.UserNotFoundException(
                            "Utilisateur non trouvé: " + username
                    ));

            // Vérifier que le compte est actif
            if (!user.getEnabled()) {
                throw new BankingException.UnauthorizedOperationException(
                        "Le compte est désactivé"
                );
            }

            if (!user.getAccountNonLocked()) {
                throw new BankingException.UnauthorizedOperationException(
                        "Le compte est verrouillé"
                );
            }

            // Générer un nouveau access token
            String newAccessToken = jwtUtils.generateTokenFromUsername(username, jwtUtils.getExpirationMs());

            // Créer les informations utilisateur
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .roles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()))
                    .lastLogin(user.getLastLogin())
                    .build();

            logger.info("Token rafraîchi avec succès pour l'utilisateur: {}", username);

            return AuthResponse.of(
                    newAccessToken,
                    refreshToken,
                    jwtUtils.getExpirationMs(),
                    userInfo
            );

        } catch (Exception e) {
            logger.error("Erreur lors du rafraîchissement du token: {}", e.getMessage());
            throw new BankingException(
                    "Impossible de rafraîchir le token: " + e.getMessage()
            );
        }
    }

    /**
     * Déconnecte l'utilisateur actuel (côté serveur, principalement pour le logging)
     */
    public void logout(String username) {
        logger.info("Déconnexion de l'utilisateur: {}", username);
        SecurityContextHolder.clearContext();
        logger.info("Utilisateur déconnecté: {}", username);
    }

    /**
     * Obtient l'utilisateur actuellement authentifié
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BankingException.UnauthorizedOperationException(
                    "Aucun utilisateur authentifié"
            );
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BankingException.UserNotFoundException(
                        "Utilisateur non trouvé: " + userDetails.getUsername()
                ));
    }

    /**
     * Obtient le nom d'utilisateur de l'utilisateur actuellement authentifié
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BankingException.UnauthorizedOperationException(
                    "Aucun utilisateur authentifié"
            );
        }

        return authentication.getName();
    }

    /**
     * Vérifie si un utilisateur est authentifié
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Crée les informations utilisateur pour la réponse
     */
    private AuthResponse.UserInfo createUserInfo(UserDetailsImpl userDetails) {
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet());

        return AuthResponse.UserInfo.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .roles(roles)
                .lastLogin(LocalDateTime.now())
                .build();
    }

    /**
     * Change le mot de passe de l'utilisateur
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        logger.info("Tentative de changement de mot de passe pour: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BankingException.UserNotFoundException(
                        "Utilisateur non trouvé: " + username
                ));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Ancien mot de passe incorrect pour: {}", username);
            throw new BadCredentialsException("Ancien mot de passe incorrect");
        }

        // Encoder et sauvegarder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Mot de passe changé avec succès pour: {}", username);
    }
}
