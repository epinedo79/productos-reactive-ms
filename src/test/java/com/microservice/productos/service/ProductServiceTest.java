package com.microservice.productos.service;

import com.microservice.productos.domain.Product;
import com.microservice.productos.dto.ProductRequest;
import com.microservice.productos.exception.DuplicateSkuException;
import com.microservice.productos.exception.ProductNotFoundException;
import com.microservice.productos.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository repository;
    ProductService service;
    Product product;

    @BeforeEach void setUp() {
        service = new ProductService(repository);
        product = new Product(1L,"SKU-001","Laptop","Laptop de prueba",new BigDecimal("2500.00"),10,true);
    }

    private ProductRequest request(String sku, String name, String description) {
        return new ProductRequest(sku,name,description,new BigDecimal("80.00"),10,null);
    }

    @Test void shouldFindAll() {
        when(repository.findAll()).thenReturn(Flux.just(product));
        StepVerifier.create(service.findAll()).expectNextMatches(x -> x.id().equals(1L)).verifyComplete();
    }

    @Test void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Mono.just(product));
        StepVerifier.create(service.findById(1L)).expectNextMatches(x -> x.name().equals("Laptop")).verifyComplete();
    }

    @Test void shouldFailFindById() {
        when(repository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(service.findById(99L)).expectError(ProductNotFoundException.class).verify();
    }

    @Test void shouldCreate() {
        when(repository.findBySku("SKU-002")).thenReturn(Mono.empty());
        when(repository.save(any(Product.class))).thenReturn(Mono.just(new Product(2L,"SKU-002","Mouse","",new BigDecimal("80"),20,true)));
        StepVerifier.create(service.create(request("SKU-002","Mouse",null))).expectNextMatches(x -> x.id().equals(2L) && x.active()).verifyComplete();
        verify(repository).save(any(Product.class));
    }

    @Test void shouldRejectDuplicateCreate() {
        when(repository.findBySku("SKU-001")).thenReturn(Mono.just(product));
        StepVerifier.create(service.create(request("SKU-001","Otro","desc"))).expectError(DuplicateSkuException.class).verify();
        verify(repository,never()).save(any());
    }

    @Test void shouldUpdate() {
        var r = request("SKU-001","Laptop actualizada","Nueva");
        when(repository.findById(1L)).thenReturn(Mono.just(product));
        when(repository.findBySku("SKU-001")).thenReturn(Mono.just(product));
        when(repository.save(any(Product.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        StepVerifier.create(service.update(1L,r)).expectNextMatches(x -> x.name().equals("Laptop actualizada")).verifyComplete();
    }

    @Test void shouldRejectDuplicateUpdate() {
        var other = new Product(2L,"SKU-002","Otro","",new BigDecimal("10"),1,true);
        when(repository.findById(1L)).thenReturn(Mono.just(product));
        when(repository.findBySku("SKU-002")).thenReturn(Mono.just(other));
        StepVerifier.create(service.update(1L,request("SKU-002","Laptop","desc"))).expectError(DuplicateSkuException.class).verify();
        verify(repository,never()).save(any());
    }

    @Test void shouldAllowSameSkuUpdate() {
        when(repository.findById(1L)).thenReturn(Mono.just(product));
        when(repository.findBySku("SKU-001")).thenReturn(Mono.just(product));
        when(repository.save(any(Product.class))).thenReturn(Mono.just(product));
        StepVerifier.create(service.update(1L,request("SKU-001","Laptop","desc"))).expectNextCount(1).verifyComplete();
    }

    @Test void shouldFailUpdateMissing() {
        when(repository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(service.update(99L,request("SKU-001","Laptop","desc"))).expectError(ProductNotFoundException.class).verify();
    }

    @Test void shouldDelete() {
        when(repository.findById(1L)).thenReturn(Mono.just(product));
        when(repository.delete(product)).thenReturn(Mono.empty());
        StepVerifier.create(service.delete(1L)).verifyComplete();
        verify(repository).delete(product);
    }

    @Test void shouldFailDeleteMissing() {
        when(repository.findById(99L)).thenReturn(Mono.empty());
        StepVerifier.create(service.delete(99L)).expectError(ProductNotFoundException.class).verify();
    }

}
