package com.agroya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotBlank
    private String municipioEnvio;

    @NotBlank
    private String direccionEnvio;

    @NotEmpty
    private List<OrderItemRequest> items;
}
