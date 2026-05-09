# API de Cupons

API REST para gerenciamento de cupons com validações de regra de negócio, soft delete e documentação OpenAPI.

## Execução rápida

### Pré-requisitos
- Java 21+
- Maven 3.9+ (ou usar `mvnw`)

### Subir aplicação local
```bash
bash mvnw spring-boot:run
```

Aplicação disponível em:
- API: `http://localhost:8080/api/coupons`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator: `http://localhost:8080/actuator`

## Testes

### Rodar todos os testes
```bash
bash mvnw test
```

### Rodar testes específicos
```bash
bash mvnw -Dtest=CreateCouponServiceTest test
bash mvnw -Dtest=GetCouponServiceTest test
bash mvnw -Dtest=DeleteCouponServiceTest test
bash mvnw -Dtest=CouponControllerIntegrationTest test
```

### Cobertura (JaCoCo)
```bash
bash mvnw clean test jacoco:report
```

Relatório em:
- `target/site/jacoco/index.html`

## Build

```bash
bash mvnw clean package
```

Artefato gerado em:
- `target/coupon-0.0.1-SNAPSHOT.jar`

## Docker

### Build da imagem
```bash
docker build -t coupon-api .
```

### Subir com docker compose
```bash
docker compose up --build
```

## Endpoints principais

Base path: `/api/coupons`

### Criar cupom
`POST /api/coupons`

Exemplo de payload:
```json
{
  "code": "ABC-123",
  "description": "10% de desconto",
  "discountValue": 10.50,
  "expirationDate": "2026-12-31T23:59:59",
  "published": true
}
```

### Buscar cupom por código
`GET /api/coupons/{code}`

### Listar cupons
`GET /api/coupons?includeDeleted=false`

### Soft delete por código
`DELETE /api/coupons/{code}`

## Regras de negócio

### Criação
- `code`:
  - normaliza removendo caracteres especiais
  - converte para maiúsculo
  - deve ter exatamente 6 caracteres alfanuméricos após normalização
- `description`:
  - obrigatória
  - máximo de 500 caracteres
- `discountValue`:
  - obrigatório
  - mínimo `0.5`
- `expirationDate`:
  - obrigatória
  - não pode estar no passado

### Exclusão
- exclusão lógica (soft delete)
- não permite deletar cupom já deletado

## Arquitetura atual

Estrutura principal:

```text
src/main/java/com/desafio/coupon
├── adapter
│   ├── persistence
│   │   ├── entity
│   │   │   └── CouponEntity
│   │   └── repository
│   │       └── CouponRepository
│   └── rest
│       ├── controller
│       │   ├── CouponApi
│       │   └── CouponController
│       ├── dto
│       │   ├── CreateCouponRequest
│       │   ├── CouponResponse
│       │   └── ErrorResponse
│       └── exception
│           └── GlobalExceptionHandler
├── application
│   ├── domain
│   │   └── Coupon
│   ├── dto
│   │   └── CouponDto
│   ├── exception
│   │   └── ... exceções de domínio
│   ├── mapper
│   │   └── CouponMapper
│   └── service
│       ├── CreateCouponService
│       ├── GetCouponService
│       └── DeleteCouponService
├── configuration
│   ├── OpenApiConfig
│   └── TimeConfig
└── CouponApplication
```

Diretriz de acoplamento:
- camada REST não depende de `CouponEntity`
- serviços retornam `CouponDto`
- mapeamento para resposta HTTP ocorre no controller

## Stack técnica
- Java 21
- Spring Boot 4.0.6
- Spring Web
- Spring Data JPA
- H2 (runtime)
- Spring Validation
- Springdoc OpenAPI
- JUnit 5
- JaCoCo

## Observações
- Banco padrão do projeto em testes/local: H2 em memória.
- Para detalhes de contratos, usar Swagger UI.
