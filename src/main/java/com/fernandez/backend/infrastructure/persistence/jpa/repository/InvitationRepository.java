package com.fernandez.backend.infrastructure.persistence.jpa.repository;

import com.fernandez.backend.domain.model.Invitation;
import com.fernandez.backend.domain.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    // Comprobación de duplicados (Requerido por tus tests)
    boolean existsByEmailAndStatus(String email, InvitationStatus status);

    // Listado general ordenado
    List<Invitation> findAllByOrderByCreatedAtDesc();

    // El "Motor" de tus filtros: permite buscar por uno o varios estados (Ej: PENDING + ACCEPTED)
    List<Invitation> findByStatusInOrderByCreatedAtDesc(List<InvitationStatus> statuses);

    // Específicos para los tests Gherkin existentes
    List<Invitation> findByStatusOrderByCreatedAtDesc(InvitationStatus status);
    List<Invitation> findByStatusNotOrderByCreatedAtDesc(InvitationStatus status);


    long countByStatus(InvitationStatus status);
}