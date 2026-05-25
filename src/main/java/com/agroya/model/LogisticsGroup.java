package com.agroya.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "logistica_agrupada")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String municipio;
    
    private LocalDate fechaProgramada;

    @Enumerated(EnumType.STRING)
    private LogisticsStatus estado;

    @ManyToMany
    @JoinTable(
        name = "logistica_pedidos",
        joinColumns = @JoinColumn(name = "logistica_id"),
        inverseJoinColumns = @JoinColumn(name = "pedido_id")
    )
    private Set<Order> pedidos = new HashSet<>();

    public enum LogisticsStatus {
        PROGRAMADO, EN_TRANSITO, COMPLETADO, CANCELADO
    }
}
