package com.agroya.controller;

import com.agroya.dto.request.LoginRequest;
import com.agroya.dto.request.SignupRequest;
import com.agroya.dto.response.JwtResponse;
import com.agroya.dto.response.MessageResponse;
import com.agroya.model.Role;
import com.agroya.model.User;
import com.agroya.repository.RoleRepository;
import com.agroya.repository.UserRepository;
import com.agroya.security.AuthTokenFilter;
import com.agroya.security.JwtUtils;
import com.agroya.security.UserDetailsImpl;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador de autenticación.
 *
 * Cambio clave vs el original:
 *   POST /api/auth/signin → además de devolver el JWT en el JSON,
 *   lo guarda en la HttpSession bajo la clave SESSION_JWT_KEY.
 *
 *   Así, cuando Thymeleaf renderiza sec:authorize="isAuthenticated()",
 *   el AuthTokenFilter ya encontró el JWT en sesión y cargó el usuario
 *   en el SecurityContext → el navbar muestra el estado real.
 *
 *   POST /api/auth/signout → invalida la sesión en el servidor.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired AuthenticationManager authenticationManager;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtils jwtUtils;

    /**
     * Login: autentica, genera JWT, lo guarda en sesión Y lo devuelve en JSON.
     * El navegador lo guarda en sessionStorage para fetch(); el servidor lo usa
     * para sec:authorize en Thymeleaf.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest,
                                              HttpSession session) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // ★ CLAVE: guardar JWT en sesión para que Thymeleaf / sec:authorize funcione ★
        session.setAttribute(AuthTokenFilter.SESSION_JWT_KEY, jwt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    /**
     * Logout explícito desde la API.
     * Limpia el JWT de la sesión pero la deja activa (para el locale i18n).
     * El endpoint /logout de Spring Security invalida la sesión completa.
     */
    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        session.removeAttribute(AuthTokenFilter.SESSION_JWT_KEY);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Sesión cerrada correctamente."));
    }

    /**
     * Registro de nuevo usuario (sin cambios respecto al original).
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: El correo ya está registrado."));
        }

        User user = new User();
        user.setNombre(signUpRequest.getNombre());
        user.setApellido(signUpRequest.getApellido());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setTelefono(signUpRequest.getTelefono());
        user.setMunicipio(signUpRequest.getMunicipio());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null || strRoles.isEmpty()) {
            Role compradorRole = roleRepository.findByName("ROLE_COMPRADOR")
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
            roles.add(compradorRole);
        } else {
            strRoles.forEach(role -> {
                String roleName = switch (role.toLowerCase()) {
                    case "admin"     -> "ROLE_ADMIN";
                    case "productor" -> "ROLE_PRODUCTOR";
                    default          -> "ROLE_COMPRADOR";
                };
                Role r = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Rol " + roleName + " no encontrado."));
                roles.add(r);
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario registrado exitosamente."));
    }
}