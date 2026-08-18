# Classificação (nova rota)

Guia rápido de como funciona a **tabela de classificação** de campeonatos,
consumindo a nova rota da API Futebols:

`GET {baseUrl}campeonato/{id}/classificacao`

---

## 1) O que o usuário vê

- Ao **selecionar um campeonato** (na faixa horizontal do topo), aparece na
  coluna lateral uma nova opção **"Classificação"** **acima de "HOJE"**.
- Clicando em **"Classificação"**, o espaço central troca a lista de jogos pela
  **tabela de classificação** do campeonato selecionado:
  - Linha de cabeçalho fixa: `#`, Time, J, V, E, D, Pts.
  - Uma linha por posição: número, logo + nome do time e estatísticas.
  - Linhas focáveis (navegação com controle remoto / TV).
  - Barras de zona de promoção (ex.: "LIBERTADORES", "SUDAMERICANA") entre os
    blocos — **não focáveis**: a navegação D-pad **pula automaticamente** essas
    barras e o cabeçalho (fix na versão `1.8`).
- Para voltar aos jogos: clique em **"HOJE"** ou em qualquer data da coluna.
- Se o campeonato não tiver tabela (ex.: mata-mata), aparece a mensagem
  "Sem tabela de classificação para este campeonato.".

> A opção "Classificação" só existe quando um campeonato está selecionado, pois a
> classificação é por campeonato. Sem seleção, a coluna mostra apenas as datas
> (como antes).

---

## 2) Como o dado chega até a tela (fluxo completo)

```
Usuário clica em um campeonato  (AdpterCat → buscarJogosPorId)
      │
      ▼
ActivityEsporte guarda campSelecionadoId e recria a coluna lateral
      │   (opção "Classificação" inserida ACIMA de "HOJE")
      ▼
Usuário clica em "Classificação"  (DataAdapter → buscarClassificacao)
      │
      ▼
ApiClassificacaoCaller  →  GET {baseUrl}campeonato/{id}/classificacao
      │   (Retrofit + Gson; aceita array direto ou objeto com array dentro)
      ▼
ClassificacaoDatabase (Room) — cache por campId (ClassificacaoDao)
      │
      ▼
ActivityEsporte lê do banco (com tentativas) → setClassificacao
      │
      ▼
ClassificacaoAdapter  →  api_classificacao_header + api_item_classificacao
```

---

## 3) Arquivos envolvidos

| Arquivo | Papel |
|---|---|
| `jogos/item/ItemClassificacao.java` | Modelo de uma linha da tabela (posição, time, pts, J/V/E/D…). |
| `jogos/interfac/ServiceClassificacao.java` | Interface Retrofit (`@GET @Url` + Bearer). |
| `jogos/response/ApiClassificacaoCaller.java` | Chama a API, converte o JSON e salva no Room. |
| `jogos/bancoSql/ClassificacaoDao.java` | Queries do cache (`getPorCamp`, `limparPorCamp`…). |
| `jogos/bancoSql/ClassificacaoDatabase.java` | Banco Room `classificacao.db`. |
| `jogos/adapter/ClassificacaoAdapter.java` | Adapter da tabela (posição 0 = cabeçalho); `isFocavel(int)` diz se a posição é focável (header e barras de promoção = `false`). |
| `jogos/item/DataItem.java` | Item genérico da coluna lateral (data ou "Classificação"). |
| `jogos/adapter/DataAdapter.java` | Coluna lateral: insere "Classificação" acima de "HOJE" e destaca o ativo. |
| `jogos/ActivityEsporte.java` | Estado do camp selecionado + `buscarClassificacao`/`setClassificacao`; `navegarListaJogos()` pula posições não focáveis da classificação (fix 1.8). |
| `jogos/adapter/AdpterCat.java` | Destaca o campeonato selecionado. |
| `res/layout/api_classificacao_header.xml` | Cabeçalho fixo da tabela (**não focável**). |
| `res/layout/api_item_classificacao.xml` | Linha da tabela. |
| `res/layout/api_item_classificacao_promocao.xml` | Barra de zona de promoção entre blocos (**não focável**; pulada na navegação). |
| `res/layout/api_item_classificacao_opcao.xml` | Opção "Classificação" da coluna lateral (padrão de `api_item_data.xml`: `tvData`, `bg_data_item`, `_36sdp`, `widget_text`). |
| `res/drawable/bg_classificacao_item.xml` | Sem uso desde a padronização da opção (mantido). |
| `res/drawable/ic_trophy_white.xml` | Sem uso desde a padronização da opção (mantido). |

---

## 4) Como o JSON é interpretado (importante!)

O `ApiClassificacaoCaller.parseClassificacao()` aceita **dois formatos**:

1. **Array direto:**
```json
[ { "posicao": 1, "time": { "nome": "Flamengo", "logo": "..." }, "pontos": 30, ... } ]
```

2. **Objeto com array dentro** (chave detectada automaticamente):
```json
{ "classificacao": [ ... ] }
```
Chaves aceitas: `classificacao`, `tabela`, `times`, `data`, `resultado`, `classification`.

Os nomes de campos mapeados em `ItemClassificacao` seguem o padrão snake_case
com alternativas camelCase (`gols_pro`/`golsPro`, etc.). O objeto `time` é
"achatado" para as colunas `time_name` e `logo`.

> **Se o JSON real for diferente**, ajuste os `@SerializedName` em
> `ItemClassificacao.java` (e as chaves em `parseClassificacao` se preciso).

---

## 5) Dicas para mudanças comuns

- **Mudar as colunas da tabela:** edite `api_classificacao_header.xml` (rótulos)
  e `api_item_classificacao.xml` (valores) + `ClassificacaoAdapter` (binding).
  Mantenha larguras iguais entre cabeçalho e linhas.
- **Mudar o título da opção:** string `opcao_classificacao` em `strings.xml`.
- **Mudar as cores:** `colors.xml` (prefixo `classificacao_`).
- **A opção usa o layout das datas:** `api_item_classificacao_opcao.xml` segue o
  padrão de `api_item_data.xml` (`bg_data_item`, `_36sdp`, fonte `widget_text`).
- **Navegação por blocos (TV):** `navegarListaJogos()` (`ActivityEsporte`) **pula
  posições não focáveis** quando o adapter é `ClassificacaoAdapter` — itens com
  `isFocavel() == false` (posição 0 = cabeçalho e as barras de promoção) são
  ignorados e a rolagem `scrollToPosition` + `focarItemAposLayout` segue até a
  próxima posição focável. Se criar uma nova barra/secção, **não** coloque
  `android:focusable="true"` no layout que o skip continua funcionando (fix 1.8).
- **Quando a opção aparece:** em `ActivityEsporte.recicleDate()` — é adicionada
  quando `campSelecionadoId != -1`.

---

## 6) Observações

- A opção "Classificação" não altera o comportamento das datas: clicar numa data mostra
  os jogos daquele dia (sem filtro de campeonato), como já funcionava.
- O cache é por `campId`; ao buscar a classificação de outro campeonato, o dado
  anterior não interfere (`limparPorCamp`).
- Tratamento de 401 igual às demais rotas (exibe o dialog de expiração).
