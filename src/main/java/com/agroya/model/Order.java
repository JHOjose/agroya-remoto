package com.agroya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private User comprador;

    private LocalDateTime fecha;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus estado;

    private BigDecimal total;

    private String municipioEnvio;
    private String direccionEnvio;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
        if (estado == null) estado = OrderStatus.PENDIENTE;
    }

    public enum OrderStatus {
        PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO
    }
}
