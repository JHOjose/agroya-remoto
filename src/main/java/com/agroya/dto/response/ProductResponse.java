package com.agroya.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Double stock;
    private String unidad;
    private String imageUrl;
    private String categoriaNombre;
    private String productorNombre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
