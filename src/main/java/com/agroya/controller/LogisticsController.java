package com.agroya.controller;

import com.agroya.dto.request.LogisticsGroupRequest;
import com.agroya.dto.response.LogisticsGroupDetailResponse;
import com.agroya.dto.response.LogisticsGroupResponse;
import com.agroya.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid; //
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // Para que funcione HttpStatus.CREATED
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; //  Para el control de roles @PreAuthorize
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

    @PostMapping("/grupos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear ruta de despacho",
            description = "Agrupa todos los pedidos pendientes de un municipio en una ruta")
    public ResponseEntity<LogisticsGroupDetailResponse> createGroup(
            @Valid @RequestBody LogisticsGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(logisticsService.createGroup(request));
    }

    @GetMapping("/grupos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar rutas activas")
    public ResponseEntity<List<LogisticsGroupDetailResponse>> getActiveGroups() {
        return ResponseEntity.ok(logisticsService.getActiveGroups());
    }

    @GetMapping("/grupos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Detalle de una ruta")
    public ResponseEntity<LogisticsGroupDetailResponse> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(logisticsService.getGroupById(id));
    }

    @PatchMapping("/grupos/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado de una ruta",
            description = "Transiciones válidas: PROGRAMADO→EN_TRANSITO, EN_TRANSITO→COMPLETADO")
    public ResponseEntity<LogisticsGroupDetailResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(logisticsService.updateGroupStatus(id, estado));
    }
}