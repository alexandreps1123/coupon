# ADR-002: Mapeamento por Fronteira (Sem Mapper Estático Global)

- Status: Accepted
- Data: 2026-05-09
- Decisores: Time de backend

## Contexto

O projeto utilizava um mapper utilitário estático único. Esse padrão facilitou início rápido, mas aumentou acoplamento implícito entre camadas e dificultou manutenção conforme o sistema evoluiu.

Foi identificado risco de violações de fronteira (ex.: camadas consumindo mapeamentos fora do contexto adequado).

## Decisão

Separar mapeamento por fronteira e usar componentes injetáveis.

- `CouponPersistenceMapper`:
  - `Coupon <-> CouponEntity`
  - `CouponEntity -> CouponDto`
- `CouponApiMapper`:
  - `CouponDto -> CouponResponse`

Não usar classe de mapper estático global para múltiplas fronteiras.

## Consequências

### Positivas

- Fronteiras explícitas e menor acoplamento.
- Maior clareza de responsabilidade por camada.
- Melhor extensibilidade para novos mapeamentos/contextos.

### Negativas

- Mais classes para manter.
- Pequeno overhead de wiring por injeção.

## Alternativas consideradas

1. Manter mapper estático único:
- Prós: menos boilerplate inicial.
- Contras: degrada manutenção em escala, facilita acoplamento indevido.

2. Mapper por caso de uso (muitos mappers pequenos):
- Prós: alta especialização.
- Contras: granularidade excessiva para o tamanho atual do projeto.
