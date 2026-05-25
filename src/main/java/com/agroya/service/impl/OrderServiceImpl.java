package com.agroya.service.impl;

import com.agroya.dto.request.OrderItemRequest;
import com.agroya.dto.request.OrderRequest;
import com.agroya.dto.request.OrderUpdateRequest; // <-- IMPORTANTE: Este import es obligatorio aquí
import com.agroya.dto.response.OrderItemResponse;
import com.agroya.dto.response.OrderResponse;
import com.agroya.exception.ResourceNotFoundException;
import com.agroya.model.Order;
import com.agroya.model.OrderItem;
import com.agroya.model.Product;
import com.agroya.model.User;
import com.agroya.repository.OrderRepository;
import com.agroya.repository.ProductRepository;
import com.agroya.repository.UserRepository;
import com.agroya.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String buyerEmail) {
        User buyer = userRepository.findByEmail(buyerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + buyerEmail));

        Order order = new Order();
        order.setComprador(buyer);
        order.setMunicipioEnvio(
                request.getMunicipioEnvio()
                        .trim()
                        .toUpperCase()
        );
        order.setDireccionEnvio(request.getDireccionEnvio());
        order.setEstado(Order.OrderStatus.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemReq.getProductoId()));

            if (product.getStock() < itemReq.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + product.getNombre() +
                        " (Disponible: " + product.getStock() + ", Solicitado: " + itemReq.getCantidad() + ")");
            }

            // Descontar stock
            product.setStock(product.getStock() - itemReq.getCantidad());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProducto(product);
            orderItem.setCantidad(itemReq.getCantidad());
            orderItem.setPrecioUnitario(product.getPrecio());
            items.add(orderItem);

            BigDecimal subtotal = product.getPrecio().multiply(BigDecimal.valueOf(itemReq.getCantidad()));
            total = total.add(subtotal);
        }

        order.setItems(items);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    /* ── updateOrder: solo dirección/municipio, solo si PENDIENTE ── */
    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request, String buyerEmail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado: " + id));

        // Solo el comprador dueño o un admin puede editar
        if (!order.getComprador().getEmail().equals(buyerEmail)) {
            throw new AccessDeniedException("No tienes permiso para editar este pedido.");
        }
        if (order.getEstado() != Order.OrderStatus.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se pueden editar pedidos en estado PENDIENTE. Estado actual: "
                            + order.getEstado());
        }

        order.setMunicipioEnvio(
                request.getMunicipioEnvio()
                        .trim()
                        .toUpperCase()
        );
        order.setDireccionEnvio(request.getDireccionEnvio());
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !order.getComprador().getEmail().equals(email)) {
            throw new AccessDeniedException("No tienes permiso para cancelar este pedido");
        }

        if (order.getEstado() == Order.OrderStatus.CANCELADO) {
            throw new IllegalStateException("El pedido ya está cancelado");
        }

        if (order.getEstado() != Order.OrderStatus.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar un pedido en estado PENDIENTE");
        }

        // 1. Cambiamos el estado
        order.setEstado(Order.OrderStatus.CANCELADO);

        // 2. Devolvemos el stock a los productos originalmente comprados
        for (OrderItem item : order.getItems()) {
            Product product = item.getProducto();
            product.setStock(product.getStock() + item.getCantidad());
            productRepository.save(product);
        }

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByProducer(Long producerId) {
        return orderRepository.findByProductorId(producerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByBuyer(String buyerEmail) {
        return orderRepository.findByCompradorEmail(buyerEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + id));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productoId(item.getProducto().getId())
                        .productoNombre(item.getProducto().getNombre())
                        .imageUrl(item.getProducto().getImageUrl())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .subtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .compradorNombre(order.getComprador().getNombre() + " " + order.getComprador().getApellido())
                .fecha(order.getFecha())
                .estado(order.getEstado().name())
                .total(order.getTotal())
                .municipioEnvio(order.getMunicipioEnvio())
                .direccionEnvio(order.getDireccionEnvio())
                .items(itemResponses)
                .build();
    }
}