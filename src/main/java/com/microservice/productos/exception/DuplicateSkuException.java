package com.microservice.productos.exception;

public class DuplicateSkuException extends RuntimeException {

    public DuplicateSkuException(String sku) { super("Ya existe un producto con SKU: " + sku); }
}
