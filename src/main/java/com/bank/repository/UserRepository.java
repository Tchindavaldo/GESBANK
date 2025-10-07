package com.bank.repository;

import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par son nom d'utilisateur ou son email
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les utilisateurs actifs
     */
    List<User> findByEnabledTrue();

    /**
     * Trouve tous les utilisateurs désactivés
     */
    List<User> findByEnabledFalse();

    /**
     * Trouve tous les utilisateurs verrouillés
     */
    List<User> findByAccountNonLockedFalse();

    /**
     * Trouve les utilisateurs créés après une certaine date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Trouve les utilisateurs par nom et prénom
     */
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * Trouve un utilisateur avec ses comptes chargés
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.id = :userId")
    Optional<User> findByIdWithAccounts(@Param("userId") Long userId);

    /**
     * Trouve un utilisateur par nom d'utilisateur avec ses comptes
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.accounts WHERE u.username = :username")
    Optional<User> findByUsernameWithAccounts(@Param("username") String username);

    /**
     * Compte le nombre d'utilisateurs actifs
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true")
    long countActiveUsers();

    /**
     * Trouve les utilisateurs qui se sont connectés dans les derniers jours
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Recherche d'utilisateurs par mot-clé (nom, prénom, username, email)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(@Param("keyword") String keyword);
}
