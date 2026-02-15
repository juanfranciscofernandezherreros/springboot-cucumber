package com.fernandez.backend.application.service;

import com.fernandez.backend.application.port.in.IAuditService;
import com.fernandez.backend.domain.model.Invitation;
import com.fernandez.backend.domain.model.Role;
import com.fernandez.backend.domain.model.User;
import com.fernandez.backend.shared.constants.ServiceStrings;
import com.fernandez.backend.shared.dto.AuditLogDto;
import com.fernandez.backend.shared.dto.InvitationAuditSnapshotDto;
import com.fernandez.backend.shared.dto.UserAuditSnapshotDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class AuditService implements IAuditService {

    private final EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<AuditLogDto> getGlobalAuditHistory() {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<AuditLogDto> history = new ArrayList<>();

        List<Class<?>> monitoredEntities = List.of(User.class, Invitation.class);

        for (Class<?> entityClass : monitoredEntities) {
            List<Object[]> results = (List<Object[]>) reader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .getResultList();

            for (Object[] row : results) {
                Object entity = row[0];
                DefaultRevisionEntity revision = (DefaultRevisionEntity) row[1];
                RevisionType revisionType = (RevisionType) row[2];

                history.add(AuditLogDto.builder()
                        .revInfo(ServiceStrings.Audit.REV_PREFIX + revision.getId())
                        .timestamp(revision.getRevisionDate().toString())
                        .author(ServiceStrings.Audit.DEFAULT_AUTHOR)
                        .operation(revisionType.name())
                        .entityName(entityClass.getSimpleName())
                        .description(extractIdentifier(entity))
                        .snapshot(toSnapshot(entity))
                        .build());
            }
        }

        history.sort(Comparator.comparing(AuditLogDto::timestamp).reversed());
        return history;
    }

    private Object toSnapshot(Object entity) {
        if (entity instanceof User u) {
            return UserAuditSnapshotDto.builder()
                    .id(u.getId())
                    .name(u.getName())
                    .email(u.getEmail())
                    .roles(u.getRoles().stream().map(Role::getName).toList())
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

    private String extractIdentifier(Object entity) {
        if (entity instanceof User u) return ServiceStrings.Audit.DESC_USER_PREFIX + u.getEmail();
        if (entity instanceof Invitation i) return ServiceStrings.Audit.DESC_INV_PREFIX + i.getEmail();
        return ServiceStrings.Audit.DESC_ID_PREFIX + entity.hashCode();
    }
}
