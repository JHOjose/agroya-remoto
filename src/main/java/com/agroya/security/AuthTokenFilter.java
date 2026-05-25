package com.agroya.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT corregido: lee el token desde DOS fuentes:
 *
 *  1. Header "Authorization: Bearer <token>"  → peticiones fetch() del navegador (API REST)
 *  2. Sesión HTTP ("_jwt" en HttpSession)      → peticiones normales de Thymeleaf
 *
 * Esto resuelve el problema "fantasma": sec:authorize en Thymeleaf necesita que
 * el SecurityContext esté cargado ANTES de renderizar la vista. Como Thymeleaf
 * no manda el header Authorization, lo leemos desde la sesión donde login.html
 * lo guardó al autenticarse.
 */
public class AuthTokenFilter extends OncePerRequestFilter {

    /** Clave donde guardamos el JWT en HttpSession */
    public static final String SESSION_JWT_KEY = "_agroya_jwt";

    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);        // 1. header Bearer
            if (jwt == null) {
                jwt = parseJwtFromSession(request); // 2. fallback: sesión HTTP
            }

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // JWT válido → cargar usuario normalmente
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                // ★ JWT expirado, inválido o ausente → limpiar sesión y contexto ★
                // Si el JWT es inválido pero existe en la sesión, lo borramos.
                // Siempre limpiamos el SecurityContext para evitar el "usuario fantasma"
                // si la sesión de Spring Security sobrevive.
                if (jwt != null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        session.removeAttribute(SESSION_JWT_KEY);
                    }
                }
                SecurityContextHolder.clearContext();
            }

        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(SESSION_JWT_KEY);
            }
        }
        filterChain.doFilter(request, response);
    }

    /** Lee el JWT del header Authorization: Bearer <token> */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    /**
     * Lee el JWT de la sesión HTTP.
     * Solo si la sesión ya existe (getSession(false)) para no crear sesiones vacías.
     */
    private String parseJwtFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object token = session.getAttribute(SESSION_JWT_KEY);
        return (token instanceof String s && StringUtils.hasText(s)) ? s : null;
    }
}