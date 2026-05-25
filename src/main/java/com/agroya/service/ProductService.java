package com.agroya.service;

import com.agroya.dto.request.ProductRequest;
import com.agroya.dto.response.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request, String producerEmail);
    ProductResponse updateProduct(Long id, ProductRequest request, String producerEmail);
    void deleteProduct(Long id, String producerEmail);
    List<ProductResponse> getProductsByProducer(Long producerId);
    List<ProductResponse> getProductsByProducerByEmail(String email);
}
