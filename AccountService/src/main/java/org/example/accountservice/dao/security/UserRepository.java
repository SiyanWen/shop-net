package org.example.accountservice.dao.security;

import org.example.accountservice.entity.security.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author b1go
 * @date 6/26/22 3:57 PM
 * Updated with correct package names
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * @param email
     * @return Optional
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);


    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
