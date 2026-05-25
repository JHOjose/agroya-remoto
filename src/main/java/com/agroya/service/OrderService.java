package com.agroya.service;

import com.agroya.dto.request.OrderRequest;
import com.agroya.dto.response.OrderResponse;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request, String buyerEmail);
    List<OrderResponse> getOrdersByProducer(Long producerId);
    List<OrderResponse> getOrdersByBuyer(String buyerEmail);
    OrderResponse getOrderById(Long id);
}
