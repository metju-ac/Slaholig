# Slaholig

## Aplikace pro komplexní distribuci pečeného cukroví

![Slaholig mascot](https://github.com/user-attachments/assets/21b0012d-1c89-461b-95ea-e27fd7abd821)

## Running the Application

Start all services using Docker Compose:

```bash
docker compose up
```

## API Documentation (Swagger UI)

Once the services are running, you can access the interactive API documentation for each microservice:

### Product Selection Service
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

### Payment Service
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8081/api-docs

### Product Delivery Service
- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8082/api-docs

### Courier Service
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8083/api-docs

The Swagger UI provides interactive documentation where you can explore all available endpoints and test API calls directly from your browser.

## Monitoring & Infrastructure

Once the services are running, you can access the following monitoring and infrastructure tools:

### Grafana Dashboard
- **URL**: http://localhost:3000/d/spring-microservices
- **Credentials**: username `admin`, password `admin`
- Pre-configured datasources for Prometheus and Loki

### Prometheus
- **URL**: http://localhost:9090
- Metrics and monitoring

### Axon Server Dashboard
- **URL**: http://localhost:8024
- Event store and message routing

### Loki
- **URL**: http://localhost:3100
- Log aggregation (accessible via Grafana)

### PostgreSQL Databases
- **Product Selection DB**: `localhost:5436`
- **Payment DB**: `localhost:5437`
- **Product Delivery DB**: `localhost:5438`
- **Courier DB**: `localhost:5439`
- **Credentials for all databases**: username `admin`, password `admin`, database `db`
