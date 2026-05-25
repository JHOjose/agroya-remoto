package com.agroya.service;

import com.agroya.dto.request.LogisticsGroupRequest;
import com.agroya.dto.response.LogisticsGroupResponse;
import com.agroya.dto.response.LogisticsGroupDetailResponse;
import java.util.List;

public interface LogisticsService {

    // ── Vista agrupada (ya existía) ──────────────────────
    /** Agrupa pedidos PENDIENTE por municipio (vista previa antes de crear ruta) */
    List<LogisticsGroupResponse> getGroupedPendingOrders();

    /** Pedidos PENDIENTE de un municipio específico */
    List<LogisticsGroupResponse> getGroupedOrdersByMunicipio(String municipio);

    // ── Gestión de rutas (NUEVO) ─────────────────────────
    /** Crea una ruta de despacho para un municipio y asigna sus pedidos pendientes */
    LogisticsGroupDetailResponse createGroup(LogisticsGroupRequest request);

    /** Lista todas las rutas activas (PROGRAMADO + EN_TRANSITO) */
    List<LogisticsGroupDetailResponse> getActiveGroups();

    /** Cambia el estado de una ruta (PROGRAMADO → EN_TRANSITO → COMPLETADO) */
    LogisticsGroupDetailResponse updateGroupStatus(Long groupId, String newStatus);

    /** Detalle de una ruta específica */
    LogisticsGroupDetailResponse getGroupById(Long groupId);
}