package com.agroya.service;

import com.agroya.dto.request.OrderItemRequest;
import com.agroya.dto.request.OrderRequest;
import com.agroya.dto.response.OrderResponse;
import com.agroya.model.Order;
import com.agroya.model.Product;
import com.agroya.model.User;
import com.agroya.repository.OrderRepository;
import com.agroya.repository.ProductRepository;
import com.agroya.repository.UserRepository;
import com.agroya.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User buyer;
    private Product product;
    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setId(1L);
        buyer.setEmail("buyer@test.com");
        buyer.setNombre("Juan");
        buyer.setApellido("Perez");

        product = new Product();
        product.setId(1L);
        product.setNombre("Papa Sabanera");
        product.setPrecio(new BigDecimal("2000"));
        product.setStock(100.0);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductoId(1L);
        itemRequest.setCantidad(10.0);

        orderRequest = new OrderRequest();
        orderRequest.setMunicipioEnvio("Bogotá");
        orderRequest.setDireccionEnvio("Calle 123");
        orderRequest.setItems(List.of(itemRequest));
    }

    @Test
    void createOrder_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse response = orderService.createOrder(orderRequest, "buyer@test.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(90.0, product.getStock()); // Stock discounted
        assertTrue(new BigDecimal("20000").compareTo(response.getTotal()) == 0);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void createOrder_InsufficientStock() {
        product.setStock(5.0); // Less than requested 10.0
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(buyer));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(orderRequest, "buyer@test.com");
        });

        verify(orderRepository, never()).save(any(Order.class));
    }
}
