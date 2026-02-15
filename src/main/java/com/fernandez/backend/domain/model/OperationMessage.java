package com.fernandez.backend.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "operation_messages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"msg_key", "lang"})
)
public class OperationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clave estable (p.ej. admin.user.updated). */
    @Column(name = "msg_key", nullable = false)
    private String key;

    /** Idioma IETF (p.ej. es, en, es-ES). */
    @Column(name = "lang", nullable = false)
    private String lang;

    /** Plantilla de mensaje, permite String.format (p.ej. "Usuario %s creado"). */
    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    public OperationMessage(String key, String lang, String text) {
        this.key = key;
        this.lang = lang;
        this.text = text;
    }
}

