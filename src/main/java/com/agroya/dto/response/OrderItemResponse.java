package com.agroya.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Double cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
