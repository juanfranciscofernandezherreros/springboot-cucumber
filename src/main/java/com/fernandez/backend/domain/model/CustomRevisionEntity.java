package com.fernandez.backend.domain.model;

import com.fernandez.backend.domain.listener.UserRevisionListener;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@Entity
@Table(name = "revinfo_custom")
@RevisionEntity(UserRevisionListener.class)
@Getter
@Setter
public class CustomRevisionEntity extends DefaultRevisionEntity {

    private String modifierUser;
    private String ipAddress;
}