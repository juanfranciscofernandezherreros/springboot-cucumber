package com.fernandez.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "privileges")
@Getter
@Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String name;

    @ManyToMany(fetch = FetchType.EAGER) // Eager para que al cargar el Rol ya traiga sus permisos
    @JoinTable(
            name = "roles_privileges",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    @ToString.Exclude
    private Set<Privilege> privileges = new HashSet<>();

    @ManyToMany(mappedBy = "privileges")
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();
}
