package com.agroya.service.impl;

import com.agroya.dto.response.LogisticsGroupResponse;
import com.agroya.dto.response.OrderSummaryResponse;
import com.agroya.model.Order;
import com.agroya.repository.OrderRepository;
import com.agroya.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsGroupResponse> getGroupedPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByEstado(Order.OrderStatus.PENDIENTE);
        
        // Agrupar por municipio de envío (filtrando nulos por seguridad)
        Map<String, List<Order>> groupedByMunicipio = pendingOrders.stream()
                .filter(o -> o.getMunicipioEnvio() != null)
                .collect(Collectors.groupingBy(Order::getMunicipioEnvio));

        return groupedByMunicipio.entrySet().stream()
                .map(entry -> mapToGroupResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LogisticsGroupResponse> getGroupedOrdersByMunicipio(String municipio) {
        List<Order> orders = orderRepository.findByMunicipioEnvioAndEstado(municipio, Order.OrderStatus.PENDIENTE);
        return List.of(mapToGroupResponse(municipio, orders));
    }

    private LogisticsGroupResponse mapToGroupResponse(String municipio, List<Order> orders) {
        List<OrderSummaryResponse> orderSummaries = orders.stream()
                .map(order -> OrderSummaryResponse.builder()
                        .id(order.getId())
                        .compradorNombre(order.getComprador() != null ? 
                                order.getComprador().getNombre() + " " + order.getComprador().getApellido() : "Desconocido")
                        .fecha(order.getFecha())
                        .total(order.getTotal())
                        .direccionEnvio(order.getDireccionEnvio())
                        .build())
                .collect(Collectors.toList());

        return LogisticsGroupResponse.builder()
                .municipio(municipio)
                .totalPedidos(orders.size())
                .pedidos(orderSummaries)
                .build();
    }
}
