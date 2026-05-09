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

## Observabilidade

### Endpoints úteis

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

Exemplos:

```bash
curl -s http://localhost:8080/actuator/metrics
curl -s http://localhost:8080/actuator/metrics/coupon.create.requests
curl -s http://localhost:8080/actuator/metrics/coupon.delete.requests
curl -s http://localhost:8080/actuator/prometheus
```

### Métricas de domínio

#### `coupon.create.requests`
Contador de tentativas de criação com tag `outcome`.

Outcomes:
- `created`: cupom criado com sucesso.
- `idempotent_hit`: requisição repetida (mesmo payload para o mesmo `code`) retornou recurso existente.
- `conflict_existing_code`: conflito por `code` já existente com payload diferente.

Interpretação rápida:
- `created` alto e estável: fluxo saudável de criação.
- aumento de `idempotent_hit`: clientes fazendo retry/repetição (esperado em cenários distribuídos).
- aumento de `conflict_existing_code`: possível colisão de códigos ou problema de integração de cliente.

#### `coupon.delete.requests`
Contador de tentativas de deleção com tag `outcome`.

Outcomes:
- `deleted`: soft delete realizado.
- `already_deleted`: tentativa de deletar cupom já deletado.
- `not_found`: cupom não encontrado para o `code` informado.

Interpretação rápida:
- `deleted` predominante: comportamento esperado.
- aumento de `already_deleted`: retries de delete ou chamadas duplicadas.
- aumento de `not_found`: cliente com `code` incorreto/desatualizado ou integração inconsistente.

### Correlação de requisição

A API aceita e devolve `X-Request-Id`.

- Se o header for enviado pelo cliente, ele é reutilizado.
- Se não for enviado, a aplicação gera automaticamente.
- O `requestId` também é incluído nos logs para facilitar troubleshooting.
