package com.fernandez.backend.controller;

import com.fernandez.backend.dto.CreateInvitationRequest;
import com.fernandez.backend.model.Invitation;
import com.fernandez.backend.model.InvitationStatus;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/invitations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InvitationController {

    private final InvitationRepository invitationRepository;

    // =====================================================
    // GESTIÓN DE LISTADOS Y FILTROS
    // =====================================================

    /**
     * Endpoint Maestro: Lista todo o filtra por estados específicos.
     * Uso: /all?statuses=PENDING,ACCEPTED
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getAll(
            @RequestParam(required = false) List<InvitationStatus> statuses) {

        if (statuses == null || statuses.isEmpty()) {
            return ResponseEntity.ok(invitationRepository.findAllByOrderByCreatedAtDesc());
        }
        return ResponseEntity.ok(invitationRepository.findByStatusInOrderByCreatedAtDesc(statuses));
    }

    /**
     * Retorna todos los tipos de estado disponibles en el sistema.
     */
    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<InvitationStatus>> getAvailableStatuses() {
        return ResponseEntity.ok(Arrays.asList(InvitationStatus.values()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getPendingInvitations() {
        return ResponseEntity.ok(invitationRepository.findByStatusOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getHistory() {
        return ResponseEntity.ok(invitationRepository.findByStatusNotOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    // =====================================================
    // ACCIONES (CREACIÓN Y ESTADOS)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createInvitation(@RequestBody CreateInvitationRequest request) {

        if (invitationRepository.existsByEmailAndStatus(
                request.email(), InvitationStatus.PENDING)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe una invitación pendiente para este correo.");
        }

        Invitation invitation = Invitation.builder()
                .email(request.email())
                .name(request.name())
                .description(request.description())
                .role("USER")
                .token(UUID.randomUUID().toString())
                .status(InvitationStatus.PENDING)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(48, ChronoUnit.HOURS))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationRepository.save(invitation));
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam InvitationStatus newStatus) {

        return invitationRepository.findById(id)
                .map(invitation -> {
                    if (!invitation.getStatus().canTransitionTo(newStatus)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Transición no permitida: de " + invitation.getStatus() + " a " + newStatus);
                    }

                    invitation.setStatus(newStatus);
                    invitationRepository.save(invitation);
                    return ResponseEntity.ok("Estado actualizado a " + newStatus);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // ELIMINACIÓN Y MODIFICACIÓN
    // =====================================================

    /**
     * Elimina una invitación permanentemente.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:delete')")
    @Transactional
    public ResponseEntity<Void> deleteInvitation(@PathVariable Long id) {
        if (!invitationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invitationRepository.deleteById(id);
        log.info("Invitación con ID {} eliminada por el administrador", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Modifica los datos de una invitación existente.
     * Normalmente se restringe a campos informativos, no al email o token.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    public ResponseEntity<?> updateInvitation(
            @PathVariable Long id,
            @RequestBody CreateInvitationRequest request) {

        return invitationRepository.findById(id)
                .map(invitation -> {
                    invitation.setName(request.name());
                    invitation.setDescription(request.description());
                    // Si permites cambiar el email, recuerda validar duplicados aquí

                    invitationRepository.save(invitation);
                    log.info("Invitación {} actualizada con éxito", id);
                    return ResponseEntity.ok(invitation);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}