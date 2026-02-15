package com.fernandez.backend.application.util;

import com.fernandez.backend.infrastructure.persistence.jpa.repository.OperationMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Resuelve mensajes operacionales (i18n) guardados en BD.
 *
 * Resolución:
 * - prueba lang exacto (p.ej. es-ES)
 * - prueba lang base (p.ej. es)
 * - fallback a "es"
 * - si no existe, devuelve la key
 */
@Component
@RequiredArgsConstructor
public class OperationMessages {

    private final OperationMessageRepository repository;

    public String get(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return get(locale, key, args);
    }

    public String get(Locale locale, String key, Object... args) {
        String langTag = (locale != null ? locale.toLanguageTag() : null);
        String lang = (locale != null ? locale.getLanguage() : null);

        String template = null;
        if (langTag != null && !langTag.isBlank()) {
            template = repository.findByKeyAndLang(key, langTag).map(m -> m.getText()).orElse(null);
        }
        if (template == null && lang != null && !lang.isBlank()) {
            template = repository.findByKeyAndLang(key, lang).map(m -> m.getText()).orElse(null);
        }
        if (template == null) {
            template = repository.findByKeyAndLang(key, "es").map(m -> m.getText()).orElse(null);
        }
        if (template == null) {
            return key;
        }

        try {
            return (args == null || args.length == 0) ? template : String.format(template, args);
        } catch (Exception e) {
            // si la plantilla no matchea args, devolvemos template bruto
            return template;
        }
    }
}

