package com.microservice.productos.controller;

import com.microservice.productos.dto.ProductRequest;
import com.microservice.productos.dto.ProductResponse;
import com.microservice.productos.exception.GlobalExceptionHandler;
import com.microservice.productos.exception.ProductNotFoundException;
import com.microservice.productos.service.ProductService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock ProductService service;
    WebTestClient client;
    ProductResponse response;

    @BeforeEach void setUp() {
        client = WebTestClient.bindToController(new ProductController(service))
            .controllerAdvice(new GlobalExceptionHandler()).build();
        response = new ProductResponse(1L,"SKU-001","Laptop","Laptop de prueba",new BigDecimal("2500.00"),10,true);
    }

    @Test void shouldGetAll() {
        when(service.findAll()).thenReturn(Flux.just(response));
        client.get().uri("/api/v1/products").exchange().expectStatus().isOk()
            .expectBody().jsonPath("$[0].id").isEqualTo(1).jsonPath("$[0].sku").isEqualTo("SKU-001");
    }

    @Test void shouldGetById() {
        when(service.findById(1L)).thenReturn(Mono.just(response));
        client.get().uri("/api/v1/products/1").exchange().expectStatus().isOk()
            .expectBody().jsonPath("$.name").isEqualTo("Laptop");
    }

    @Test void shouldReturn404() {
        when(service.findById(99L)).thenReturn(Mono.error(new ProductNotFoundException(99L)));
        client.get().uri("/api/v1/products/99").exchange().expectStatus().isNotFound()
            .expectBody().jsonPath("$.status").isEqualTo(404);
    }

    @Test void shouldCreate() {
        when(service.create(any(ProductRequest.class))).thenReturn(Mono.just(response));
        client.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON).bodyValue("""
            {"sku":"SKU-001","name":"Laptop","description":"Laptop de prueba","price":2500.00,"stock":10,"active":true}
            """).exchange().expectStatus().isCreated().expectBody().jsonPath("$.id").isEqualTo(1);
    }

    @Test void shouldValidateCreate() {
        client.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON).bodyValue("""
            {"sku":"","name":"","price":0,"stock":-1}
            """).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.status").isEqualTo(400);
    }

    @Test void shouldUpdate() {
        when(service.update(eq(1L),any(ProductRequest.class))).thenReturn(Mono.just(response));
        client.put().uri("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON).bodyValue("""
            {"sku":"SKU-001","name":"Laptop actualizada","description":"Nueva","price":2600.00,"stock":8,"active":true}
            """).exchange().expectStatus().isOk();
    }

    @Test void shouldDelete() {
        when(service.delete(1L)).thenReturn(Mono.empty());
        client.delete().uri("/api/v1/products/1").exchange().expectStatus().isNoContent();
        verify(service).delete(1L);
    }
}
