package com.duoc.gestiondespachos.config;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ROLE_PREFIX = "ROLE_";

    private static final String ROL_DESCARGA_GUIAS = "DESCARGA_GUIAS";
    private static final String ROL_GESTOR_GUIAS = "GESTOR_GUIAS";

    private static final String AUTH_DESCARGA_GUIAS = ROLE_PREFIX + ROL_DESCARGA_GUIAS;
    private static final String AUTH_GESTOR_GUIAS = ROLE_PREFIX + ROL_GESTOR_GUIAS;

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_SCOPES = "scp";
    private static final String CLAIM_CONSULTA_ROLE_SUFFIX = "consultaRole";
    private static final String CLAIM_EXTENSION_ROLE = "extension_role";

    private static final String SCOPE_GUIAS_DESCARGAR = "guias.descargar";
    private static final String SCOPE_GUIAS_GESTIONAR = "guias.gestionar";

    @Bean
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                // API REST stateless con autenticación mediante Bearer Token/JWT.
                // No se utilizan sesiones ni cookies de navegador para autenticar solicitudes.
                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Consola H2 solo para desarrollo local.
                        .requestMatchers("/h2-console/**").permitAll()

                        // Rol de descarga: solo puede descargar guías.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/guias/*/s3",
                                "/api/guias/*/descargar"
                        ).hasRole(ROL_DESCARGA_GUIAS)

                        // Rol gestor: puede usar el resto de endpoints de guías.
                        .requestMatchers("/api/guias/**")
                        .hasRole(ROL_GESTOR_GUIAS)

                        // Cualquier otra ruta requiere autenticación.
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))

                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Claim estándar si Azure AD B2C entrega roles directamente.
            agregarRolesDesdeClaim(authorities, jwt.getClaim(CLAIM_ROLES));
            agregarRolesDesdeClaim(authorities, jwt.getClaim(CLAIM_EXTENSION_ROLE));

            // Custom claims de Azure AD B2C.
            // La guía trabaja con extension_consultaRole, pero Azure puede emitirlo
            // con prefijos extendidos, por eso se revisan claims terminados en consultaRole.
            jwt.getClaims().forEach((claimName, claimValue) -> {
                if (claimName != null && claimName.endsWith(CLAIM_CONSULTA_ROLE_SUFFIX)) {
                    agregarRolesDesdeClaim(authorities, claimValue);
                }
            });

            // Scopes opcionales, por si Azure entrega permisos OAuth2 en el claim scp.
            agregarScopesComoRoles(authorities, jwt.getClaim(CLAIM_SCOPES));

            return authorities;
        });

        return converter;
    }

    private static void agregarRolesDesdeClaim(Collection<GrantedAuthority> authorities, Object claim) {
        if (claim instanceof Collection<?> roles) {
            for (Object role : roles) {
                agregarRol(authorities, role);
            }
            return;
        }

        if (claim instanceof String rolesTexto) {
            String[] roles = rolesTexto.split(",");

            for (String role : roles) {
                agregarRol(authorities, role);
            }
        }
    }

    private static void agregarRol(Collection<GrantedAuthority> authorities, Object role) {
        if (!(role instanceof String roleTexto)) {
            return;
        }

        String authority = normalizarRol(roleTexto);

        if (authority == null) {
            return;
        }

        authorities.add(new SimpleGrantedAuthority(authority));
    }

    private static String normalizarRol(String roleTexto) {
        if (roleTexto == null || roleTexto.isBlank()) {
            return null;
        }

        String roleLimpio = roleTexto.trim();

        return switch (roleLimpio) {
            case ROL_DESCARGA_GUIAS, AUTH_DESCARGA_GUIAS, "descarga", "descargar" ->
                    AUTH_DESCARGA_GUIAS;

            case ROL_GESTOR_GUIAS, AUTH_GESTOR_GUIAS, "gestor", "gestion", "gestionar" ->
                    AUTH_GESTOR_GUIAS;

            default -> {
                if (roleLimpio.startsWith(ROLE_PREFIX)) {
                    yield roleLimpio;
                }

                yield ROLE_PREFIX + roleLimpio;
            }
        };
    }

    private static void agregarScopesComoRoles(Collection<GrantedAuthority> authorities, Object scopesClaim) {
        if (!(scopesClaim instanceof String scopesTexto)) {
            return;
        }

        String[] scopes = scopesTexto.split(" ");

        for (String scope : scopes) {
            switch (scope.trim()) {
                case SCOPE_GUIAS_DESCARGAR ->
                        authorities.add(new SimpleGrantedAuthority(AUTH_DESCARGA_GUIAS));

                case SCOPE_GUIAS_GESTIONAR ->
                        authorities.add(new SimpleGrantedAuthority(AUTH_GESTOR_GUIAS));

                default -> {
                    // Scope no utilizado por esta API.
                }
            }
        }
    }
}