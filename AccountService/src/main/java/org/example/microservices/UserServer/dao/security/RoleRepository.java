package org.example.microservices.UserServer.dao.security;

import org.example.microservices.UserServer.entity.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author b1go
 * @date 6/26/22 4:03 PM
 * Updated with correct package names
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
