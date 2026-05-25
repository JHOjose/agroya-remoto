package com.agroya.service.impl;

import com.agroya.dto.request.ProductRequest;
import com.agroya.dto.response.ProductResponse;
import com.agroya.exception.ResourceNotFoundException;
import com.agroya.model.Category;
import com.agroya.model.Product;
import com.agroya.model.User;
import com.agroya.repository.CategoryRepository;
import com.agroya.repository.ProductRepository;
import com.agroya.repository.UserRepository;
import com.agroya.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, String producerEmail) {
        User producer = userRepository.findByEmail(producerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + producerEmail));
        
        Category category = categoryRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoriaId()));

        Product product = new Product();
        updateProductFields(product, request, category);
        product.setProductor(producer);

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        
        // Verificar si el usuario es el dueño o es ADMIN
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && !product.getProductor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tiene permiso para modificar este producto");
        }

        Category category = categoryRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada: " + request.getCategoriaId()));

        updateProductFields(product, request, category);

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !product.getProductor().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("No tiene permiso para eliminar este producto");
        }

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByProducer(Long producerId) {
        return productRepository.findByProductorId(producerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void updateProductFields(Product product, ProductRequest request, Category category) {
        product.setNombre(request.getNombre());
        product.setDescripcion(request.getDescripcion());
        product.setPrecio(request.getPrecio());
        product.setStock(request.getStock());
        product.setUnidad(request.getUnidad());
        product.setImageUrl(request.getImageUrl());
        product.setCategoria(category);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .nombre(product.getNombre())
                .descripcion(product.getDescripcion())
                .precio(product.getPrecio())
                .stock(product.getStock())
                .unidad(product.getUnidad())
                .imageUrl(product.getImageUrl())
                .categoriaNombre(product.getCategoria() != null ? product.getCategoria().getName() : null)
                .productorNombre(product.getProductor() != null ? 
                        product.getProductor().getNombre() + " " + product.getProductor().getApellido() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
