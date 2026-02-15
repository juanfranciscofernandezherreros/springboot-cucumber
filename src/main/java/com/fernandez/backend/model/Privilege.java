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


    @ManyToMany(mappedBy = "privileges")
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();
}
