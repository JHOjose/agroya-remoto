package com.agroya.repository;

import com.agroya.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductorId(Long productorId);
    List<Product> findByCategoriaId(Long categoriaId);
}
