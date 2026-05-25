package com.agroya.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LogisticsGroupRequest {
    @NotBlank(message = "El municipio es obligatorio")
    private String municipio;

    @NotNull(message = "La fecha programada es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fechaProgramada;
}