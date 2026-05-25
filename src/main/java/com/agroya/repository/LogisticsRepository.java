package com.agroya.repository;

import com.agroya.model.LogisticsGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface LogisticsRepository extends JpaRepository<LogisticsGroup, Long> {

    // Todos los grupos por estado (para listar PROGRAMADO, EN_TRANSITO, etc.)
    List<LogisticsGroup> findByEstadoOrderByFechaProgramadaAsc(
            LogisticsGroup.LogisticsStatus estado);

    // Todos los grupos activos (no completados ni cancelados)
    @Query("SELECT l FROM LogisticsGroup l WHERE l.estado IN ('PROGRAMADO','EN_TRANSITO') " +
            "ORDER BY l.fechaProgramada ASC")
    List<LogisticsGroup> findAllActive();

    // ¿Ya existe un grupo PROGRAMADO para este municipio?
    boolean existsByMunicipioAndEstado(String municipio, LogisticsGroup.LogisticsStatus estado);

    // Busca grupos por municipio (ya existía, mejorada)
    @Query("SELECT l FROM LogisticsGroup l WHERE l.municipio = :municipio " +
            "AND l.estado = 'PROGRAMADO' ORDER BY l.fechaProgramada ASC")
    List<LogisticsGroup> findOptimizedByMunicipio(@Param("municipio") String municipio);
}