package com.agroya.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderUpdateRequest {

    @NotBlank(message = "El municipio es obligatorio")
    private String municipioEnvio;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;
}