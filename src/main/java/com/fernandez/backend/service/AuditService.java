package com.fernandez.backend.service;

import com.fernandez.backend.dto.AuditLogDto;
import com.fernandez.backend.dto.InvitationAuditSnapshotDto;
import com.fernandez.backend.dto.UserAuditSnapshotDto;
import com.fernandez.backend.model.Invitation;
import com.fernandez.backend.model.Role;
import com.fernandez.backend.model.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final EntityManager entityManager;

    public List<AuditLogDto> getGlobalAuditHistory() {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<AuditLogDto> history = new ArrayList<>();

        List<Class<?>> monitoredEntities = List.of(
                User.class,
                Invitation.class
        );

        for (Class<?> entityClass : monitoredEntities) {

            List<Object[]> results = reader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .getResultList();

            for (Object[] row : results) {
                Object entity = row[0];
                DefaultRevisionEntity revision = (DefaultRevisionEntity) row[1];
                RevisionType revisionType = (RevisionType) row[2];

                history.add(AuditLogDto.builder()
                        .revInfo("REV-" + revision.getId())
                        .timestamp(revision.getRevisionDate().toString())
                        .author("admin") // Sustituir por CustomRevisionEntity si lo tienes
                        .operation(revisionType.name())
                        .entityName(entityClass.getSimpleName())
                        .description(extractIdentifier(entity))
                        .snapshot(toSnapshot(entity))
                        .build());
            }
        }

        // Ordenar: más reciente primero
        history.sort(
                Comparator.comparing(AuditLogDto::getTimestamp).reversed()
        );

        return history;
    }

    // =====================================================
    // SNAPSHOT MAPPER (CLAVE)
    // =====================================================
    private Object toSnapshot(Object entity) {

        if (entity instanceof User u) {
            return UserAuditSnapshotDto.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .roles(
                            u.getRoles().stream()
                                    .map(Role::getName)
                                    .toList()
                    )
                    .accountNonLocked(u.isAccountNonLocked())
                    .build();
        }

        if (entity instanceof Invitation i) {
            return InvitationAuditSnapshotDto.builder()
                    .id(i.getId())
                    .email(i.getEmail())
                    .status(i.getStatus().name())
                    .build();
        }

        return null;
    }

    // =====================================================
    // DESCRIPCIÓN HUMANA
    // =====================================================
    private String extractIdentifier(Object entity) {
        if (entity instanceof User u) {
            return "User: " + u.getEmail();
        }
        if (entity instanceof Invitation i) {
            return "Inv: " + i.getEmail();
        }
        return "ID: " + entity.hashCode();
    }
}
