package com.fernandez.backend.infrastructure.persistence.jpa.repository;

import com.fernandez.backend.domain.model.AppProperty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppPropertyRepository extends JpaRepository<AppProperty, Long> {
    Optional<AppProperty> findByKeyAndProfile(String key, String profile);
    Optional<AppProperty> findByKeyAndProfileIsNull(String key);
}

