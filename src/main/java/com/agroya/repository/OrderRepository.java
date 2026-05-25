package com.agroya.repository;

import com.agroya.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByEstado(Order.OrderStatus estado);

    List<Order> findByMunicipioEnvioAndEstado(
            String municipio,
            Order.OrderStatus estado);

    /**
     * Pedidos de un comprador por su email — evita el findAll() + filter en memoria
     * que existía en OrderServiceImpl.getOrdersByBuyer().
     */
    List<Order> findByCompradorEmail(String email);

    @Query("""
        SELECT o
        FROM Order o
        JOIN o.comprador u
        WHERE UPPER(u.municipio) = UPPER(:municipio)
    """)
    List<Order> findPedidosPorMunicipio(@Param("municipio") String municipio);

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN o.items i
        JOIN i.producto p
        WHERE p.productor.id = :producerId
    """)
    List<Order> findByProductorId(@Param("producerId") Long producerId);
}