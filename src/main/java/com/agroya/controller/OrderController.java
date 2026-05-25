package com.agroya.controller;

import com.agroya.dto.request.OrderRequest;
import com.agroya.dto.request.OrderUpdateRequest; // <-- NUEVO IMPORT
import com.agroya.dto.response.OrderResponse;
import com.agroya.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "API para la gestión de compras y pedidos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Crear un nuevo pedido", description = "Permite a un comprador realizar una compra de uno o varios productos")
    @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Error en la solicitud o stock insuficiente")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request, Authentication authentication) {
        return new ResponseEntity<>(orderService.createOrder(request, authentication.getName()), HttpStatus.CREATED);
    }

    @GetMapping("/productor/{producerId}")
    @PreAuthorize("hasRole('PRODUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Listar pedidos para un productor", description = "Obtiene los pedidos que contienen productos del productor especificado")
    public ResponseEntity<List<OrderResponse>> getOrdersByProducer(@PathVariable Long producerId) {
        return ResponseEntity.ok(orderService.getOrdersByProducer(producerId));
    }

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Listar pedidos del comprador autenticado")
    public ResponseEntity<List<OrderResponse>> getMyOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getOrdersByBuyer(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalles de un pedido por ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COMPRADOR')")
    @Operation(summary = "Actualizar dirección y municipio de un pedido")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request, // <-- CAMBIADO AQUÍ (De OrderRequest a OrderUpdateRequest)
            Authentication authentication) {
        return ResponseEntity.ok(orderService.updateOrder(id, request, authentication.getName()));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('COMPRADOR') or hasRole('ADMIN')")
    @Operation(summary = "Cancelar un pedido")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(orderService.cancelOrder(id, authentication.getName()));
    }
}