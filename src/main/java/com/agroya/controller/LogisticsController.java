package com.agroya.controller;

import com.agroya.dto.response.LogisticsGroupResponse;
import com.agroya.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistica")
@RequiredArgsConstructor
@Tag(name = "Logística", description = "API para la coordinación de transporte compartido")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LogisticsController {

    private final LogisticsService logisticsService;

    @GetMapping("/agrupar")
    @Operation(summary = "Agrupar pedidos pendientes por municipio", 
               description = "Obtiene una lista de grupos de logística basados en los municipios de destino de los pedidos pendientes.")
    public ResponseEntity<List<LogisticsGroupResponse>> getGroupedPendingOrders() {
        return ResponseEntity.ok(logisticsService.getGroupedPendingOrders());
    }

    @GetMapping("/agrupar/{municipio}")
    @Operation(summary = "Agrupar pedidos de un municipio específico")
    public ResponseEntity<List<LogisticsGroupResponse>> getGroupedOrdersByMunicipio(@PathVariable String municipio) {
        return ResponseEntity.ok(logisticsService.getGroupedOrdersByMunicipio(municipio));
    }
}
