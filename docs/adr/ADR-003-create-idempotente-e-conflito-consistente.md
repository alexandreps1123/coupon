# ADR-003: Create Idempotente por Code e Tratamento Consistente de Conflito

- Status: Accepted
- Data: 2026-05-09
- Decisores: Time de backend

## Contexto

A abordagem `existsByCode + save` é vulnerável a race condition sob concorrência: duas requisições podem passar na checagem e competir no insert.

Além disso, sem política explícita de idempotência, requisições repetidas com mesmo payload podem falhar de forma inconsistente.

## Decisão

Adotar create idempotente por `code` com semântica explícita:

1. Se já existe cupom com mesmo `code`:
- Se payload é semanticamente igual e cupom não está deletado: retornar recurso existente (idempotência).
- Se payload diverge: lançar `CouponCodeAlreadyExistsException`.

2. Se não existe:
- Tentar `save` diretamente.
- Em `DataIntegrityViolationException` (corrida de unicidade), reler por `code` com retry curto para capturar commit concorrente e aplicar a mesma regra idempotência/conflito.

## Consequências

### Positivas

- Remove dependência frágil de `existsByCode + save`.
- Comportamento previsível para requisição repetida.
- Conflito semântico consistente em payload divergente.

### Negativas

- Lógica de criação fica mais complexa.
- Retry curto adiciona custo e precisa ser monitorado.

## Alternativas consideradas

1. Manter `existsByCode + save`:
- Prós: simples.
- Contras: inconsistente sob concorrência.

2. Resolver apenas por constraint do banco sem idempotência semântica:
- Prós: simples no domínio.
- Contras: pior experiência para chamadas repetidas legítimas.

3. Chave de idempotência externa (header/token):
- Prós: semântica mais robusta para APIs distribuídas.
- Contras: aumenta escopo além da necessidade atual do projeto.
