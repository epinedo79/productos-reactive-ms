package com.microservice.productos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;


public record ProductRequest(
    @NotBlank(message="El SKU es obligatorio") @Size(max=50, message="El SKU no puede superar 50 caracteres") String sku,
    @NotBlank(message="El nombre es obligatorio") @Size(max=150, message="El nombre no puede superar 150 caracteres") String name,
    @Size(max=500, message="La descripción no puede superar 500 caracteres") String description,
    @NotNull(message="El precio es obligatorio") @DecimalMin(value="0.01", message="El precio debe ser mayor a 0") BigDecimal price,
    @NotNull(message="El stock es obligatorio") @Min(value=0, message="El stock no puede ser negativo") Integer stock,
    Boolean active) { }
