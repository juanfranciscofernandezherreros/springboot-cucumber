package com.fernandez.backend.config;

import com.fernandez.backend.service.ILogoutService;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize en los controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final ILogoutService logoutHandler;
    private final CorsConfigurationSource corsConfigurationSource;

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
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(
                                "/", "/index.html", "/static/**", "/auth/**",
                                "/swagger-ui/**", "/v3/api-docs/**"
                        ).permitAll()

                        // --- B. ENDPOINTS DE INVITACIÓN (invitation-controller) ---
                        // El registro de una nueva invitación suele ser público (solicitud)
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/invitations").permitAll()
                        // El resto de la gestión requiere privilegios
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/invitations/**").hasAuthority("admin:read")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/admin/invitations/**").hasAuthority("admin:update")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/invitations/**").hasAuthority("admin:update")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/invitations/**").hasAuthority("admin:delete")

                        // --- C. DASHBOARD DE ADMINISTRACIÓN (admin-controller) ---
                        // Lectura de datos, estados y estadísticas
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/admin/users",
                                "/api/v1/admin/user-status",
                                "/api/v1/admin/stats",
                                "/api/v1/admin/locked-users").hasAuthority("admin:read")

                        // Acciones de modificación (Bloqueo, Desbloqueo, Roles)
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/admin/lock-user/**",
                                "/api/v1/admin/unlock/**",
                                "/api/v1/admin/create-user").hasAnyAuthority("admin:update", "admin:create")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/admin/update-user/**",
                                "/api/v1/admin/update-role").hasAuthority("admin:update")

                        // Eliminación de usuarios
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/delete/**").hasAuthority("admin:delete")

                        // --- D. AUDITORÍA (audit-controller) ---
                        .requestMatchers("/api/v1/admin/audit/**").hasAuthority("admin:read")

                        // --- E. ENDPOINTS DE USUARIO (user-controller) ---
                        .requestMatchers("/api/v1/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/update").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/me/password").authenticated()

                        // --- F. REGLA DE RESPALDO PARA ADMIN ---
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

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
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            SecurityContextHolder.clearContext();
                            response.setStatus(200);
                        })
                );

        return http.build();
    }
}
