package com.fernandez.backend.infrastructure.persistence.jpa.repository;

import com.fernandez.backend.domain.model.OperationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperationMessageRepository extends JpaRepository<OperationMessage, Long> {
    Optional<OperationMessage> findByKeyAndLang(String key, String lang);
}

