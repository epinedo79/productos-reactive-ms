package com.microservice.productos.exception;

import com.microservice.productos.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> notFound(ProductNotFoundException ex, ServerWebExchange e) {
        log.info("Producto no encontrado. path={}, message={}",e.getRequest().getPath(),ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), e, List.of());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public Mono<ResponseEntity<ErrorResponse>> duplicate(DuplicateSkuException ex, ServerWebExchange e) {
        log.info("SKU duplicado. path={}, message={}",e.getRequest().getPath(),ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), e, List.of());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> validation(WebExchangeBindException ex, ServerWebExchange e) {
        var details = ex.getFieldErrors().stream()
            .map(x -> x.getField() + ": " + x.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, "Error de validación", e, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> constraint(ConstraintViolationException ex, ServerWebExchange e) {
        var details = ex.getConstraintViolations().stream()
            .map(x -> x.getPropertyPath() + ": " + x.getMessage()).toList();

        log.info("Error de validación. path={}, details={}", e.getRequest().getPath(),details);
        return build(HttpStatus.BAD_REQUEST, "Error de validación", e, details);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> generic(Exception ex, ServerWebExchange e) {
        log.info("Error interno no controlado. path={}", e.getRequest().getPath(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno", e, List.of());
    }

    private Mono<ResponseEntity<ErrorResponse>> build(HttpStatus status, String message,
                                                        ServerWebExchange e, List<String> details) {
        var body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                                     message, e.getRequest().getPath().value(), details);
        return Mono.just(ResponseEntity.status(status).body(body));
    }

}
