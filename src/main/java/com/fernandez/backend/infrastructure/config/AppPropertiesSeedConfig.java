package com.fernandez.backend.infrastructure.config;

import com.fernandez.backend.domain.model.AppProperty;
import com.fernandez.backend.infrastructure.persistence.jpa.repository.AppPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppPropertiesSeedConfig {

    private final AppPropertyRepository repository;

    @Bean
    public ApplicationRunner seedAppProperties() {
        return args -> {
            // CORS (guardamos listas como CSV)
            upsert("app.cors.mapping", null, "/**");
            upsert("app.cors.allowed-origins", null, "http://localhost:3000,http://localhost:8087");
            upsert("app.cors.allowed-methods", null, "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            upsert("app.cors.allowed-headers", null, "*");
            upsert("app.cors.exposed-headers", null, "Authorization");
            upsert("app.cors.allow-credentials", null, "true");

            // Seguridad (durations) - ejemplo
            // Nota: el mapa actual vive en app.security.lock.durations.* en YAML.
            // Aquí dejo una forma de externalizarlo si quieres: key->value por fila.
            upsert("app.security.lock.durations.1", null, "60000");
            upsert("app.security.lock.durations.2", null, "120000");
            upsert("app.security.lock.durations.3", null, "180000");
        };
    }

    private void upsert(String key, String profile, String value) {
        repository.findByKeyAndProfile(key, profile)
                .or(() -> repository.findByKeyAndProfileIsNull(key).filter(p -> profile == null))
                .ifPresentOrElse(
                        p -> {
                            p.setValue(value);
                            repository.save(p);
                        },
                        () -> repository.save(new AppProperty(key, profile, value))
                );
    }
}

