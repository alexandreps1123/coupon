# ADR-001: Regra de Soft Delete no Domínio

- Status: Accepted
- Data: 2026-05-09
- Decisores: Time de backend

## Contexto

O fluxo de deleção de cupom já esteve distribuído entre `CouponEntity` (persistência) e `DeleteCouponService` (aplicação), com regressões no passado envolvendo inconsistência entre `insert` e `update` durante refatorações.

Como consequência, havia risco de regra de negócio ficar acoplada ao adapter de persistência e perder consistência no núcleo de domínio.

## Decisão

A regra de soft delete pertence ao domínio `Coupon`.

- `Coupon` contém estado de deleção (`deleted`, `deletedAt`).
- `Coupon` expõe o comportamento `delete(Clock)`.
- `delete(Clock)` valida estado atual e lança `CouponAlreadyDeletedException` quando aplicável.
- `DeleteCouponService` orquestra o fluxo (`Entity -> Domain -> Domain delete -> Entity`) sem reimplementar regra de negócio.

## Consequências

### Positivas

- Regra de negócio centralizada no domínio.
- Menor risco de divergência entre serviço e persistência.
- Testes de regra de deleção podem ficar mais diretos no nível de domínio.

### Negativas

- Exige mapeamento de ida e volta no fluxo de deleção.
- Aumenta responsabilidade do mapper de persistência.

## Alternativas consideradas

1. Regra de soft delete na `CouponEntity`:
- Prós: fluxo mais curto no serviço.
- Contras: regra de negócio acoplada à persistência, maior risco de regressão arquitetural.

2. Regra no serviço sem método de domínio:
- Prós: implementação rápida.
- Contras: lógica espalhada, pior manutenção e menor clareza de fronteira.
