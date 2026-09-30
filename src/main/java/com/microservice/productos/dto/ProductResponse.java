package com.microservice.productos.dto;

import java.math.BigDecimal;


public record ProductResponse(Long id, String sku, String name, String description,
                              BigDecimal price, Integer stock, Boolean active) { }
