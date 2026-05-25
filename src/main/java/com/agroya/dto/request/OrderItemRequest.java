package com.agroya.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long productoId;

    @NotNull
    @Positive
    private Double cantidad;
}
