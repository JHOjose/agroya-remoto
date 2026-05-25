package com.agroya.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsGroupResponse {
    private String municipio;
    private Integer totalPedidos;
    private List<OrderSummaryResponse> pedidos;
}
