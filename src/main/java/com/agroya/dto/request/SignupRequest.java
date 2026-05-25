package com.agroya.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private String nombre;
    private String apellido;
    private String telefono;
    private String direccion;
    private String municipio;

    private Set<String> role;
}
