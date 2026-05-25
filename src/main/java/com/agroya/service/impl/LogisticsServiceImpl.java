package com.agroya.service.impl;

import com.agroya.dto.request.LogisticsGroupRequest;
import com.agroya.dto.response.LogisticsGroupDetailResponse;
import com.agroya.dto.response.LogisticsGroupResponse;
import com.agroya.dto.response.OrderSummaryResponse;
import com.agroya.exception.ResourceNotFoundException;
import com.agroya.model.LogisticsGroup;
import com.agroya.model.LogisticsGroup.LogisticsStatus;
import com.agroya.model.Order;
import com.agroya.repository.LogisticsRepository;
import com.agroya.repository.OrderRepository;
import com.agroya.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderRepository orderRepository;
    private final LogisticsRepository logisticsRepository; // ★ ahora sí se usa

    // ── Vista previa agrupada (sin persistir) ─────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsGroupResponse> getGroupedPendingOrders() {
        List<Order> pending = orderRepository.findByEstado(Order.OrderStatus.PENDIENTE);
        Map<String, List<Order>> byMunicipio = pending.stream()
                .filter(o -> o.getMunicipioEnvio() != null)
                .collect(Collectors.groupingBy(Order::getMunicipioEnvio));

        return byMunicipio.entrySet().stream()
                .map(e -> mapToGroupResponse(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsGroupResponse> getGroupedOrdersByMunicipio(String municipio) {
        List<Order> orders = orderRepository
                .findByMunicipioEnvioAndEstado(municipio, Order.OrderStatus.PENDIENTE);
        return List.of(mapToGroupResponse(municipio, orders));
    }

    // ── Gestión de rutas (NUEVO) ──────────────────────────────

    @Override
    @Transactional
    public LogisticsGroupDetailResponse createGroup(LogisticsGroupRequest request) {
        // 1. Buscar todos los pedidos PENDIENTE del municipio
        List<Order> pedidosPendientes = orderRepository
                .findByMunicipioEnvioAndEstado(
                        request.getMunicipio(), Order.OrderStatus.PENDIENTE);

        if (pedidosPendientes.isEmpty()) {
            throw new IllegalStateException(
                    "No hay pedidos pendientes para el municipio: " + request.getMunicipio());
        }

        // 2. Calcular total estimado de la ruta
        BigDecimal totalEstimado = pedidosPendientes.stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Crear y persistir el grupo logístico
        LogisticsGroup group = new LogisticsGroup();
        group.setMunicipio(request.getMunicipio());
        group.setFechaProgramada(request.getFechaProgramada());
        group.setEstado(LogisticsStatus.PROGRAMADO);
        group.setTotalEstimado(totalEstimado);
        group.getPedidos().addAll(pedidosPendientes);

        LogisticsGroup saved = logisticsRepository.save(group);
        return mapToDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsGroupDetailResponse> getActiveGroups() {
        return logisticsRepository.findAllActive().stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LogisticsGroupDetailResponse updateGroupStatus(Long groupId, String newStatus) {
        LogisticsGroup group = logisticsRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Grupo logístico no encontrado: " + groupId));

        LogisticsStatus status;
        try {
            status = LogisticsStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Estado inválido: " + newStatus +
                            ". Valores permitidos: PROGRAMADO, EN_TRANSITO, COMPLETADO, CANCELADO");
        }

        // Validar transiciones permitidas
        validateTransition(group.getEstado(), status);

        group.setEstado(status);

        // Si se completa la ruta, marcar los pedidos como ENTREGADO
        if (status == LogisticsStatus.COMPLETADO) {
            group.getPedidos().forEach(p -> p.setEstado(Order.OrderStatus.ENTREGADO));
        }

        return mapToDetailResponse(logisticsRepository.save(group));
    }

    @Override
    @Transactional(readOnly = true)
    public LogisticsGroupDetailResponse getGroupById(Long groupId) {
        LogisticsGroup group = logisticsRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Grupo logístico no encontrado: " + groupId));
        return mapToDetailResponse(group);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void validateTransition(LogisticsStatus current, LogisticsStatus next) {
        boolean valid = switch (current) {
            case PROGRAMADO  -> next == LogisticsStatus.EN_TRANSITO
                    || next == LogisticsStatus.CANCELADO;
            case EN_TRANSITO -> next == LogisticsStatus.COMPLETADO
                    || next == LogisticsStatus.CANCELADO;
            case COMPLETADO, CANCELADO -> false; // estado final
        };
        if (!valid) {
            throw new IllegalStateException(
                    "No se puede pasar de " + current + " a " + next);
        }
    }

    private LogisticsGroupDetailResponse mapToDetailResponse(LogisticsGroup g) {
        List<OrderSummaryResponse> pedidos = g.getPedidos().stream()
                .map(o -> OrderSummaryResponse.builder()
                        .id(o.getId())
                        .compradorNombre(o.getComprador() != null
                                ? o.getComprador().getNombre() + " " + o.getComprador().getApellido()
                                : "Desconocido")
                        .fecha(o.getFecha())
                        .total(o.getTotal())
                        .direccionEnvio(o.getDireccionEnvio())
                        .build())
                .collect(Collectors.toList());

        return LogisticsGroupDetailResponse.builder()
                .id(g.getId())
                .municipio(g.getMunicipio())
                .estado(g.getEstado().name())
                .fechaProgramada(g.getFechaProgramada())
                .totalPedidos(pedidos.size())
                .totalEstimado(g.getTotalEstimado())
                .pedidos(pedidos)
                .build();
    }

    private LogisticsGroupResponse mapToGroupResponse(String municipio, List<Order> orders) {
        List<OrderSummaryResponse> summaries = orders.stream()
                .map(o -> OrderSummaryResponse.builder()
                        .id(o.getId())
                        .compradorNombre(o.getComprador() != null
                                ? o.getComprador().getNombre() + " " + o.getComprador().getApellido()
                                : "Desconocido")
                        .fecha(o.getFecha())
                        .total(o.getTotal())
                        .direccionEnvio(o.getDireccionEnvio())
                        .build())
                .collect(Collectors.toList());

        return LogisticsGroupResponse.builder()
                .municipio(municipio)
                .totalPedidos(orders.size())
                .pedidos(summaries)
                .build();
    }
}