package com.agroya.repository;

import com.agroya.model.LogisticsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogisticsRepository extends JpaRepository<LogisticsGroup, Long> {
    
    /**
     * Busca grupos de logística por municipio utilizando una consulta optimizada.
     */
    @Query(value = "SELECT * FROM logistica_agrupada l WHERE l.municipio = :municipio AND l.estado = 'PROGRAMADO' ORDER BY l.fecha_programada ASC", nativeQuery = true)
    List<LogisticsGroup> findOptimizedByMunicipio(@Param("municipio") String municipio);
}
