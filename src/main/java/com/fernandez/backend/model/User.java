package com.fernandez.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @NotAudited
    private List<Token> tokens;

    @Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Builder.Default
    private int failedAttempt = 0;

    @Builder.Default
    private int lockCount = 0;

    @Builder.Default
    private boolean accountNonLocked = true;

    private Date lockTime;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 1. Extraemos los nombres de los roles (asegurando el prefijo ROLE_)
        Set<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(
                        role.getName().startsWith("ROLE_") ? role.getName() : "ROLE_" + role.getName()
                ))
                .collect(Collectors.toSet());
        // 2. Extraemos y añadimos los privilegios de cada uno de esos roles
        roles.forEach(role -> {
            if (role.getPrivileges() != null) {
                role.getPrivileges().forEach(privilege -> {
                    authorities.add(new SimpleGrantedAuthority(privilege.getName()));
                });
            }
        });

        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

}