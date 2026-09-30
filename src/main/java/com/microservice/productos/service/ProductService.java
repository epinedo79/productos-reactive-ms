package com.microservice.productos.service;

import com.microservice.productos.domain.Product;
import com.microservice.productos.dto.ProductRequest;
import com.microservice.productos.dto.ProductResponse;
import com.microservice.productos.exception.DuplicateSkuException;
import com.microservice.productos.exception.ProductNotFoundException;
import com.microservice.productos.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Flux<ProductResponse> findAll() {
        log.info("Iniciando consulta de todos los productos");
        return repository.findAll()
                .map(this::toResponse)
                .doOnNext(product ->
                        log.debug(
                                "Producto encontrado: id={}, sku={}",
                                product.id(),
                                product.sku()
                        )
                )
                .doOnComplete(() ->
                        log.info("Consulta de productos finalizada")
                )
                .doOnError(error ->
                        log.error(
                                "Error consultando productos: {}",
                                error.getMessage(),
                                error
                        )
                );
    }

    public Mono<ProductResponse> findById(Long id) {
        log.info("Buscando producto por id={}", id);

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .map(this::toResponse)
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info(
                                "Producto encontrado correctamente: id={}, sku={}",
                                product.id(),
                                product.sku()
                        );
                    }
                })
                .doOnError(error ->
                        log.error(
                                "Error buscando producto id={}: {}",
                                id,
                                error.getMessage()
                        )
                );
    }

    public Mono<ProductResponse> create(ProductRequest request) {
        log.info("Creando producto.....");
        return repository.findBySku(request.sku())
                .flatMap(x -> Mono.<ProductResponse>error(new DuplicateSkuException(request.sku())))
                .switchIfEmpty(Mono.defer(() -> repository.save(toEntity(request)))
                        .map(this::toResponse))
                .doOnSuccess(product -> {
                    if (product != null) {
                        log.info("Producto creado correctamente: id={}, sku={}",
                                product.id(),product.sku());

                    }
                })
                .doOnError(error ->
                        log.error( "Error creando producto sku={}: {}",
                                request.sku(), error.getMessage())
                );
    }

    public Mono<ProductResponse> update(Long id, ProductRequest request) {
        log.info("Actualizando producto ...");

        return repository.findById(id)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new ProductNotFoundException(id))
                ))
                .flatMap(x ->
                        validateSkuForUpdate(id, request.sku())
                                .then(Mono.defer(() ->
                                        repository.save(toEntity(id, request))
                                ))
                )
                .map(this::toResponse)
                .doOnSuccess(product -> {

                    if (product != null) {
                        log.info(
                                "Producto actualizado correctamente: id={}",
                                product.id()
                        );
                    }
                })
                .doOnError(error ->
                        log.error(
                                "Error actualizando producto id={}: {}",
                                id,
                                error.getMessage()
                        )
                );
    }

    public Mono<Void> delete(Long id) {
        log.info("Eliminando producto ... ");
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
                .flatMap(repository::delete)
                .doOnSuccess(ignored ->
                        log.info("Producto eliminado correctamente: id={}", id)
                )
                .doOnError(error ->
                        log.error("Error eliminando producto id={}: {}", id, error.getMessage())
                );
    }

    private Mono<Void> validateSkuForUpdate(Long id, String sku) {
        log.info("Validando sku ... ");
        return repository.findBySku(sku)
                .filter(found -> !found.id().equals(id))
                .flatMap(found -> Mono.<Void>error(new DuplicateSkuException(sku)))
                .then();
    }

    private Product toEntity(ProductRequest r) {
        return new Product(null, r.sku().trim(), r.name().trim(),
                Optional.ofNullable(r.description()).map(String::trim).orElse(""),
                r.price(), r.stock(), Optional.ofNullable(r.active()).orElse(true));
    }

    private Product toEntity(Long id, ProductRequest r) {
        return new Product(id, r.sku().trim(), r.name().trim(),
                Optional.ofNullable(r.description()).map(String::trim).orElse(""),
                r.price(), r.stock(), Optional.ofNullable(r.active()).orElse(true));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(p.id(), p.sku(), p.name(), p.description(), p.price(), p.stock(), p.active());
    }
}
