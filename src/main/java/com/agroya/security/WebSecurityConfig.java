package com.agroya.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * Configuración de Spring Security.
 *
 * Arquitectura adoptada (Thymeleaf + REST coexistiendo):
 * ──────────────────────────────────────────────────────
 *  • Las vistas Thymeleaf (/login, /register, /productos, /pedidos/**, /admin/**)
 *    son servidas por @Controller y son públicas o controladas por roles aquí.
 *
 *  • Los endpoints REST (/api/**) siguen usando JWT stateless.
 *    El navegador los llama con fetch() + Authorization: Bearer <token>.
 *
 *  • El login real sigue siendo POST /api/auth/signin → devuelve JWT.
 *    Spring Security NO gestiona el login de formulario (lo hace JS).
 *
 *  NOTA: la sesión HTTP se usa ÚNICAMENTE para el locale (i18n).
 *        El JWT vive en sessionStorage del navegador.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))

                /*
                 * Sesión REQUERIDA solo para el locale (SessionLocaleResolver).
                 * Los endpoints REST siguen siendo stateless porque el filtro JWT
                 * no depende de la sesión.
                 */
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        // ── REST: Auth público ──
                        .requestMatchers("/api/auth/**").permitAll()

                        // ── REST: Productos públicos (GET) ──
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/productos/**").permitAll()

                        // ── REST: Swagger, Jasper ──
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ── Vistas Thymeleaf públicas ──
                        .requestMatchers("/", "/index", "/home").permitAll()
                        .requestMatchers("/productos", "/productos/**").permitAll()
                        .requestMatchers("/login", "/register", "/logout-success").permitAll()

                        // ── Recursos estáticos ──
                        .requestMatchers("/css/**", "/js/**", "/images/**",
                                "/favicon.ico", "/error").permitAll()

                        // ── Admin: solo ROLE_ADMIN ──
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // ── Pedidos: autenticado ──
                        .requestMatchers("/pedidos/**").authenticated()

                        // ── REST Admin: reporte ──
                        .requestMatchers("/api/reportes/**").hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                // ── CONFIGURACIÓN DE LOGOUT (NUEVO) ──
                .logout(logout -> logout
                        .logoutUrl("/logout") // Ruta que interceptará Spring Security
                        .logoutSuccessUrl("/") // A dónde te redirige tras limpiar la sesión
                        .invalidateHttpSession(true) // Destruye la sesión (JSESSIONID) del servidor
                        .deleteCookies("JSESSIONID") // Borra la cookie del navegador
                        .permitAll()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "https://localhost:8443",   // HTTPS local
                "http://localhost:8080"     // HTTP local (dev sin SSL)
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "Accept"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}