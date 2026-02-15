package com.fernandez.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_properties", uniqueConstraints = @UniqueConstraint(columnNames = {"prop_key", "profile"}))
public class AppProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clave estable (p.ej. app.cors.mapping). */
    @Column(name = "prop_key", nullable = false)
    private String key;

    /** Perfil al que aplica (null o "default" para global; "test" para tests, etc.). */
    @Column(name = "profile")
    private String profile;

    /** Valor como string; el consumidor lo parsea (bool, long, list...). */
    @Column(name = "prop_value", nullable = false, length = 2000)
    private String value;

    public AppProperty(String key, String profile, String value) {
        this.key = key;
        this.profile = profile;
        this.value = value;
    }
}

