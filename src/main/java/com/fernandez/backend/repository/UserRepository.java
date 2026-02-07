package com.fernandez.backend.repository;

import com.fernandez.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Consulta 1: Total de usuarios
    long count();

    // Consulta 2: Usuarios bloqueados
    long countByAccountNonLockedFalse();

    // Consulta 3: Invitaciones pendientes
    long countByPasswordIsNull();

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}