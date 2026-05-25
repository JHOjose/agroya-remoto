package com.agroya.config;

import com.agroya.model.Category;
import com.agroya.model.Product;
import com.agroya.model.Role;
import com.agroya.model.User;
import com.agroya.repository.CategoryRepository;
import com.agroya.repository.ProductRepository;
import com.agroya.repository.RoleRepository;
import com.agroya.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class DataSeeder {

    // Lista maestra de categorías
    private static final List<String[]> CATEGORIAS = List.of(
            new String[]{"Frutas",      "Frutas frescas de temporada"},
            new String[]{"Verduras",    "Hortalizas y verduras de hoja"},
            new String[]{"Tubérculos",  "Papa, yuca, ñame y similares"},
            new String[]{"Cereales",    "Maíz, arroz, trigo y granos"},
            new String[]{"Lácteos",     "Leche, queso y derivados"},
            new String[]{"Carnes",      "Pollo, cerdo, res y otros"},
            new String[]{"Otros",       "Productos agrícolas varios"}
    );

    @Bean
    public CommandLineRunner initMockData(UserRepository userRepository,
                                          ProductRepository productRepository,
                                          RoleRepository roleRepository,
                                          CategoryRepository categoryRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {

            // --- 0. POBLAR CATEGORÍAS (Prioridad 1, para que los productos no fallen) ---
            if (categoryRepository.count() == 0) {
                System.out.println("🌱 Poblando tabla de Categorías...");
                CATEGORIAS.forEach(c -> {
                    Category cat = new Category();
                    cat.setName(c[0]);
                    cat.setDescription(c[1]);
                    categoryRepository.save(cat);
                });
                System.out.println("✅ Categorías inicializadas (" + CATEGORIAS.size() + ")");
            }

            // --- 1. POBLAR USUARIOS Y PRODUCTOS ---
            boolean adminExiste = userRepository.findAll().stream()
                    .anyMatch(u -> "admin@agroya.com".equals(u.getEmail()));

            if (!adminExiste) {
                System.out.println("🌱 Poblando base de datos con usuarios y productos de prueba...");

                Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElse(null);
                Role productorRole = roleRepository.findByName("ROLE_PRODUCTOR").orElse(null);
                Role compradorRole = roleRepository.findByName("ROLE_COMPRADOR").orElse(null);

                if (adminRole != null && productorRole != null && compradorRole != null) {

                    User admin = new User();
                    admin.setNombre("Super");
                    admin.setApellido("Admin");
                    admin.setEmail("admin@agroya.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRoles(Set.of(adminRole));
                    userRepository.save(admin);

                    User productor = new User();
                    productor.setNombre("Don Carlos");
                    productor.setApellido("Gómez");
                    productor.setEmail("carlos@granja.com");
                    productor.setPassword(passwordEncoder.encode("carlos123"));
                    productor.setTelefono("3201234567");
                    productor.setMunicipio("Neiva");
                    productor.setRoles(Set.of(productorRole));
                    userRepository.save(productor);

                    User comprador = new User();
                    comprador.setNombre("Restaurante");
                    comprador.setApellido("El Buen Sabor");
                    comprador.setEmail("compras@buensabor.com");
                    comprador.setPassword(passwordEncoder.encode("comprador123"));
                    comprador.setTelefono("3109876543");
                    comprador.setMunicipio("Pitalito");
                    comprador.setRoles(Set.of(compradorRole));
                    userRepository.save(comprador);

                    // Buscamos las categorías recién creadas para asignárselas a los productos
                    Category catVerduras = categoryRepository.findById(2L).orElse(null);
                    Category catOtros = categoryRepository.findById(7L).orElse(null);

                    Product tomate = new Product();
                    tomate.setNombre("Tomate Chonto");
                    tomate.setDescripcion("Tomate fresco, cosecha de esta semana.");
                    tomate.setPrecio(new BigDecimal("2500.0"));
                    tomate.setStock(100.0);
                    tomate.setUnidad("kg");
                    tomate.setProductor(productor);
                    if(catVerduras != null) tomate.setCategoria(catVerduras); // Asignamos Categoría
                    productRepository.save(tomate);

                    Product cafe = new Product();
                    cafe.setNombre("Café Tostado Especial");
                    cafe.setDescripcion("Café de exportación, origen Huila.");
                    cafe.setPrecio(new BigDecimal("18000.0"));
                    cafe.setStock(50.0);
                    cafe.setUnidad("libra");
                    cafe.setProductor(productor);
                    if(catOtros != null) cafe.setCategoria(catOtros); // Asignamos Categoría
                    productRepository.save(cafe);

                    System.out.println("✅ Datos iniciales de usuarios y productos cargados con éxito.");
                }
            } else {
                System.out.println("La base de datos ya contiene usuarios. Omitiendo poblamiento de usuarios.");
            }
        };
    }
}