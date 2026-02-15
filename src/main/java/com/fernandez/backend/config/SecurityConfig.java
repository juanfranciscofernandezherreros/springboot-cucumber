package com.fernandez.backend.config;

import com.fernandez.backend.service.ILogoutService;
import com.fernandez.backend.utils.constants.ApiPaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize en los controladores
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final ILogoutService logoutHandler;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          AuthenticationProvider authenticationProvider,
                          ILogoutService logoutHandler,
                          @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
        this.logoutHandler = logoutHandler;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                /* ============================================================
                   1. CONFIGURACIÓN BASE (CORS, CSRF & H2)
                   ============================================================ */
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                /* ============================================================
                   2. GESTIÓN DE SESIÓN (STATELESS - JWT)
                   ============================================================ */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                /* ============================================================
                   3. REGLAS DE AUTORIZACIÓN (MAPEADO DE ENDPOINTS)
                   ============================================================ */
                .authorizeHttpRequests(auth -> auth

                        // --- A. RUTAS PÚBLICAS (Auth, Documentación, H2) ---
                        .requestMatchers(new AntPathRequestMatcher(ApiPaths.Security.PUBLIC_H2_CONSOLE)).permitAll()
                        .requestMatchers(
                                ApiPaths.Security.PUBLIC_ROOT,
                                ApiPaths.Security.PUBLIC_INDEX,
                                ApiPaths.Security.PUBLIC_STATIC,
                                ApiPaths.Security.PUBLIC_AUTH,
                                ApiPaths.Security.PUBLIC_SWAGGER_UI,
                                ApiPaths.Security.PUBLIC_OPENAPI_DOCS
                        ).permitAll()

                        // --- B. ENDPOINTS DE INVITACIÓN (invitation-controller) ---
                        // El registro de una nueva invitación suele ser público (solicitud)
                        .requestMatchers(HttpMethod.POST, ApiPaths.Security.INVITATIONS_PATTERN).permitAll()
                        // El resto de la gestión requiere privilegios
                        .requestMatchers(HttpMethod.GET, ApiPaths.Security.INVITATIONS_PATTERN).hasAuthority("admin:read")
                        .requestMatchers(HttpMethod.PATCH, ApiPaths.Security.INVITATIONS_PATTERN).hasAuthority("admin:update")
                        .requestMatchers(HttpMethod.PUT, ApiPaths.Security.INVITATIONS_PATTERN).hasAuthority("admin:update")
                        .requestMatchers(HttpMethod.DELETE, ApiPaths.Security.INVITATIONS_PATTERN).hasAuthority("admin:delete")

                        // --- C. DASHBOARD DE ADMINISTRACIÓN (admin-controller) ---
                        // Lectura de datos, estados y estadísticas
                        .requestMatchers(HttpMethod.GET,
                                ApiPaths.Security.ADMIN_USERS_GET,
                                ApiPaths.Security.ADMIN_USER_STATUS_GET,
                                ApiPaths.Security.ADMIN_STATS_GET,
                                ApiPaths.Security.ADMIN_LOCKED_USERS_GET
                        ).hasAuthority("admin:read")

                        // Acciones de modificación (Bloqueo, Desbloqueo, Roles)
                        .requestMatchers(HttpMethod.POST,
                                ApiPaths.Security.ADMIN_LOCK_USER_PATTERN,
                                ApiPaths.Security.ADMIN_UNLOCK_USER_PATTERN,
                                ApiPaths.Security.ADMIN_CREATE_USER
                        ).hasAnyAuthority("admin:update", "admin:create")

                        .requestMatchers(HttpMethod.PUT,
                                ApiPaths.Security.ADMIN_UPDATE_USER_PATTERN,
                                ApiPaths.Security.ADMIN_UPDATE_ROLE
                        ).hasAuthority("admin:update")

                        // Eliminación de usuarios
                        .requestMatchers(HttpMethod.DELETE, ApiPaths.Security.ADMIN_DELETE_USER_PATTERN).hasAuthority("admin:delete")

                        // --- D. AUDITORÍA (audit-controller) ---
                        .requestMatchers(ApiPaths.Security.AUDIT_PATTERN).hasAuthority("admin:read")

                        // --- E. ENDPOINTS DE USUARIO (user-controller) ---
                        .requestMatchers(ApiPaths.Security.USERS_ME_PATTERN).authenticated()
                        .requestMatchers(HttpMethod.PUT, ApiPaths.Security.USERS_UPDATE).authenticated()
                        .requestMatchers(HttpMethod.POST, ApiPaths.Security.USERS_CHANGE_PASSWORD).authenticated()

                        // --- F. REGLA DE RESPALDO PARA ADMIN ---
                        .requestMatchers(ApiPaths.Security.ADMIN_PATTERN).hasRole("ADMIN")

                        // Cierre de seguridad
                        .anyRequest().authenticated()
                )

                /* ============================================================
                   4. FILTROS Y AUTENTICACIÓN
                   ============================================================ */
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                /* ============================================================
                   5. LOGOUT
                   ============================================================ */
                .logout(logout -> logout
                        .logoutUrl(ApiPaths.Security.LOGOUT)
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(200);
                        })
                );

        return http.build();
    }
}
