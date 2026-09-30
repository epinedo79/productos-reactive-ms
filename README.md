# Microservicio Reactivo de Gestión de Productos

## Stack

- Java 17
- Spring Boot 3.5.x
- Spring WebFlux
- Spring Data R2DBC
- H2 + R2DBC
- JUnit 5
- Mockito
- Reactor Test / StepVerifier
- JaCoCo
- Docker / Docker Compose

## Instrucciones para Compilar
Requisitos: JDK 17+ y Maven 3.9+.

Ejecutar los siguientes comandos:
```bash
mvn clean compile
```

## Instrucciones para ejecutar localmente
Requisitos: JDK 17+ y Maven 3.9+.

Ejecutar los siguientes comandos:

```bash
mvn spring-boot:run
```


## Instrucciones para ejecutar con Docker
Ejecutar los siguientes comandos:

```bash
docker build -t products-app:1.0 .
```

```bash
docker run -d --name products-app -p 8080:8080 products-app:1.0
```


## Instrucciones para ejecutar con Docker Compose
Ejecutar los siguientes comandos:

```bash
docker compose up --build
```

En segundo plano:

```bash
docker compose up -d --build
```

Detener:

```bash
docker compose down
```


## Instrucciones para ejecutar los test
Ejecutar los siguientes comandos:

```bash
mvn test
```

## Instrucciones para generar el reporte de cobertura
Ejecutar los siguientes comandos:

```bash
mvn clean verify
```
Reporte JaCoCo:

```text
target/site/jacoco/index.html
```

El `pom.xml` configura una regla mínima del 70% de cobertura de instrucciones .


## Endpoints del Microservicio
Luego de ejecutar localmente o desde Docker, los endpoints disponibles son:

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/products` | Listar productos |
| GET | `/api/v1/products/{id}` | Buscar producto |
| POST | `/api/v1/products` | Crear producto |
| PUT | `/api/v1/products/{id}` | Actualizar producto |
| DELETE | `/api/v1/products/{id}` | Eliminar producto |

Estos endpoints se pueden consumir desde POSTMAN  impprtando el archivo API_PRODUCTS.postman_collection.json
o ejecutando los comandos CURL de ejemplo.


## Ejemplos de consumo de los endpoints

### GET todos

```bash
curl http://localhost:8080/api/v1/products
```

### GET por ID

```bash
curl http://localhost:8080/api/v1/products/1
```

### POST

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku":"SKU-003",
    "name":"Teclado Mecánico",
    "description":"Teclado para desarrollo",
    "price":180.50,
    "stock":15,
    "active":true
  }'
```

### PUT

```bash
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "sku":"SKU-001",
    "name":"Laptop Lenovo Actualizada",
    "description":"Laptop empresarial",
    "price":2700.00,
    "stock":8,
    "active":true
  }'
```

### DELETE

```bash
curl -X DELETE http://localhost:8080/api/v1/products/1
```



## Manejo de errores

Se utiliza `@RestControllerAdvice` para devolver una estructura uniforme.

Ejemplo 404:

```json
{
  "timestamp": "2026-09-30T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Producto no encontrado con id: 99",
  "path": "/api/v1/products/99",
  "details": []
}
```

Ejemplo 409:

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Ya existe un producto con SKU: SKU-001"
}
```
