package com.fernandez.backend.application.util;

import com.fernandez.backend.infrastructure.persistence.jpa.repository.AppPropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Resuelve propiedades desde BD con fallback a application.yml.
 *
 * Orden:
 * 1) app_properties(key, activeProfile)
 * 2) app_properties(key, null)
 * 3) Environment (yml)
 */
@Component
@RequiredArgsConstructor
public class AppPropertyResolver {

    private final AppPropertyRepository repository;
    private final Environment environment;

    public Optional<String> getRaw(String key) {
        String profile = firstActiveProfile();

        Optional<String> byProfile = (profile == null)
                ? Optional.empty()
                : repository.findByKeyAndProfile(key, profile).map(p -> p.getValue());

        if (byProfile.isPresent()) {
            return byProfile;
        }

        Optional<String> global = repository.findByKeyAndProfileIsNull(key).map(p -> p.getValue());
        if (global.isPresent()) {
            return global;
        }

        return Optional.ofNullable(environment.getProperty(key));
    }

    public String getRequired(String key) {
        return getRaw(key).orElseThrow(() -> new IllegalStateException("Falta la propiedad: " + key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getRaw(key).map(Boolean::parseBoolean).orElse(defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return getRaw(key).map(Long::parseLong).orElse(defaultValue);
    }

    public List<String> getCsvList(String key, List<String> defaultValue) {
        return getRaw(key)
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList())
                .orElse(defaultValue);
    }

    private String firstActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) return null;
        return profiles[0];
    }
}

