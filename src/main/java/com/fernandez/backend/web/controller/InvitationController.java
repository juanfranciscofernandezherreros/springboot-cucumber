package com.fernandez.backend.web.controller;

import com.fernandez.backend.domain.model.Invitation;
import com.fernandez.backend.domain.model.InvitationStatus;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.InvitationRepository;
import com.fernandez.backend.shared.constants.ApiPaths;
 import com.fernandez.backend.shared.constants.ServiceStrings;
import com.fernandez.backend.shared.dto.CreateInvitationRequestDto;
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
@RequestMapping(ApiPaths.Invitations.BASE)
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class InvitationController {

    private final InvitationRepository invitationRepository;

    @GetMapping(ApiPaths.Invitations.ALL)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getAll(
            @RequestParam(name = "statuses", required = false) List<InvitationStatus> statuses) {

        if (statuses == null || statuses.isEmpty()) {
            return ResponseEntity.ok(invitationRepository.findAllByOrderByCreatedAtDesc());
        }
        return ResponseEntity.ok(invitationRepository.findByStatusInOrderByCreatedAtDesc(statuses));
    }

    @GetMapping(ApiPaths.Invitations.STATUSES)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<InvitationStatus>> getAvailableStatuses() {
        return ResponseEntity.ok(Arrays.asList(InvitationStatus.values()));
    }

    @GetMapping(ApiPaths.Invitations.PENDING)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getPendingInvitations() {
        return ResponseEntity.ok(invitationRepository.findByStatusOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    @GetMapping(ApiPaths.Invitations.HISTORY)
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<List<Invitation>> getHistory() {
        return ResponseEntity.ok(invitationRepository.findByStatusNotOrderByCreatedAtDesc(InvitationStatus.PENDING));
    }

    // =====================================================
    // ACCIONES (CREACIÓN Y ESTADOS)
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAuthority('admin:create')")
    @Transactional
    public ResponseEntity<?> createInvitation(@RequestBody CreateInvitationRequestDto request) {

        if (invitationRepository.existsByEmailAndStatus(
                request.email(), InvitationStatus.PENDING)) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ServiceStrings.Invitation.ERR_PENDING_EXISTS);
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


    @PatchMapping(ApiPaths.Invitations.UPDATE_STATUS)
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    public ResponseEntity<?> updateStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "newStatus") InvitationStatus newStatus) {

        return invitationRepository.findById(id)
                .map(invitation -> {
                    if (!invitation.getStatus().canTransitionTo(newStatus)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ServiceStrings.Invitation.ERR_INVALID_TRANSITION_PREFIX + invitation.getStatus() + " a " + newStatus);
                    }

                    invitation.setStatus(newStatus);
                    invitationRepository.save(invitation);
                    return ResponseEntity.ok(ServiceStrings.Invitation.OK_STATUS_UPDATED_PREFIX + newStatus);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =====================================================
    // ELIMINACIÓN Y MODIFICACIÓN
    // =====================================================

    /**
     * Elimina una invitación permanentemente.
     */
    @DeleteMapping(ApiPaths.Invitations.DELETE)
    @PreAuthorize("hasAuthority('admin:delete')")
    @Transactional
    public ResponseEntity<Void> deleteInvitation(@PathVariable(name = "id") Long id) {
        if (!invitationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        invitationRepository.deleteById(id);
        log.info(ServiceStrings.Invitation.INFO_DELETED, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Modifica los datos de una invitación existente.
     * Normalmente se restringe a campos informativos, no al email o token.
     */
    @PutMapping(ApiPaths.Invitations.UPDATE)
    @PreAuthorize("hasAuthority('admin:update')")
    @Transactional
    public ResponseEntity<?> updateInvitation(
            @PathVariable(name = "id") Long id,
             @RequestBody CreateInvitationRequestDto request) {

        return invitationRepository.findById(id)
                .map(invitation -> {
                    invitation.setName(request.name());
                    invitation.setDescription(request.description());
                    // Si permites cambiar el email, recuerda validar duplicados aquí

                    invitationRepository.save(invitation);
                    log.info(ServiceStrings.Invitation.INFO_UPDATED, id);
                    return ResponseEntity.ok(invitation);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}