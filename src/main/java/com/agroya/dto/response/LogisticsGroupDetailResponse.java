package com.agroya.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LogisticsGroupDetailResponse {
    private Long id;
    private String municipio;
    private String estado;
    private LocalDate fechaProgramada;
    private Integer totalPedidos;
    private BigDecimal totalEstimado;
    private List<OrderSummaryResponse> pedidos;
}