package com.agroya.service.impl;

import com.agroya.dto.request.OrderItemRequest;
import com.agroya.dto.request.OrderRequest;
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
        order.setMunicipioEnvio(request.getMunicipioEnvio());
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
