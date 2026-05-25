package com.agroya.service;

import com.agroya.dto.response.LogisticsGroupResponse;
import java.util.List;

public interface LogisticsService {
    List<LogisticsGroupResponse> getGroupedPendingOrders();
    List<LogisticsGroupResponse> getGroupedOrdersByMunicipio(String municipio);
}
