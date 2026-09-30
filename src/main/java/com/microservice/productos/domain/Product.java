package com.microservice.productos.domain;

import java.math.BigDecimal;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table("products")
public record Product(@Id Long id, String sku, String name, String description,
                      BigDecimal price, Integer stock, Boolean active) { }
