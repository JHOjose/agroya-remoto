package com.agroya.controller;

import com.agroya.dto.request.ProductRequest;
import com.agroya.dto.response.ProductResponse;
import com.agroya.service.ProductService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "API para la gestión de productos agrícolas")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Obtiene el catálogo completo de productos disponibles")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo producto", description = "Permite a un productor o admin registrar un nuevo producto en el catálogo")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ApiResponse(responseCode = "403", description = "No tiene permisos para realizar esta acción")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request, Authentication authentication) {
        return new ResponseEntity<>(productService.createProduct(request, authentication.getName()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto", description = "Permite actualizar los datos de un producto existente. Solo el dueño o un admin pueden hacerlo.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado")
    @ApiResponse(responseCode = "403", description = "No tiene permisos para modificar este producto")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request, Authentication authentication) {
        return ResponseEntity.ok(productService.updateProduct(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto")
    @ApiResponse(responseCode = "204", description = "Producto eliminado")
    @ApiResponse(responseCode = "403", description = "No tiene permisos para eliminar este producto")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, Authentication authentication) {
        productService.deleteProduct(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/productor/{producerId}")
    @Operation(summary = "Listar productos por productor")
    public ResponseEntity<List<ProductResponse>> getProductsByProducer(@PathVariable Long producerId) {
        return ResponseEntity.ok(productService.getProductsByProducer(producerId));
    }
}
