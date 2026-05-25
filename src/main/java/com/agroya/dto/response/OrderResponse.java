package com.agroya.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String compradorNombre;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal total;
    private String municipioEnvio;
    private String direccionEnvio;
    private List<OrderItemResponse> items;
}
