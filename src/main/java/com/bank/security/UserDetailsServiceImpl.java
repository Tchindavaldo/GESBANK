package com.bank.security;

import com.bank.model.User;
import com.bank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'implémentation de UserDetailsService pour Spring Security
 * Charge les détails de l'utilisateur depuis la base de données
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * Charge un utilisateur par son nom d'utilisateur
     * Cette méthode est appelée par Spring Security lors de l'authentification
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Tentative de chargement de l'utilisateur: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec le nom d'utilisateur: {}", username);
                    return new UsernameNotFoundException(
                            "Utilisateur non trouvé avec le nom d'utilisateur: " + username
                    );
                });

        logger.debug("Utilisateur trouvé: {}, Activé: {}, Verrouillé: {}",
                user.getUsername(), user.getEnabled(), !user.getAccountNonLocked());

        if (!user.getEnabled()) {
            logger.warn("Tentative de connexion avec un compte désactivé: {}", username);
        }

        if (!user.getAccountNonLocked()) {
            logger.warn("Tentative de connexion avec un compte verrouillé: {}", username);
        }

        return UserDetailsImpl.build(user);
    }

    /**
     * Charge un utilisateur par son nom d'utilisateur ou son email
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameNotFoundException {
        logger.debug("Tentative de chargement de l'utilisateur avec l'identifiant: {}", identifier);

        User user = userRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec l'identifiant: {}", identifier);
                    return new UsernameNotFoundException(
                            "Utilisateur non trouvé avec l'identifiant: " + identifier
                    );
                });

        logger.debug("Utilisateur trouvé: {}", user.getUsername());

        return UserDetailsImpl.build(user);
    }

    /**
     * Charge un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("Tentative de chargement de l'utilisateur avec l'ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Utilisateur non trouvé avec l'ID: {}", userId);
                    return new UsernameNotFoundException(
                            "Utilisateur non trouvé avec l'ID: " + userId
                    );
                });

        logger.debug("Utilisateur trouvé: {}", user.getUsername());

        return UserDetailsImpl.build(user);
    }
}
