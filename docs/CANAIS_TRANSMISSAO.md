# Canais de Transmissão

Guia rápido de como funciona e como mexer nos **canais de transmissão**
(feature do SDK, criada após a API `/api/jogos` passar a devolver os campos
`canais`, `canais_ia`, `canais_simples` e `canais_links`).

---

## 1) O que o usuário vê

- **Faixa de chips na lista:** cada jogo com `canais_links` mostra, abaixo da
  linha, uma faixa (rolagem horizontal) com um **chip** por link de transmissão:
  logotipo + nome do canal.
- **Clique na linha do jogo** → abre um **modal (bottom sheet)** com os
  **canais da playlist (`canais_links`)** do jogo (única seção exibida: `Canais:`).
- **Clique em um chip da faixa** → abre o **modal de detalhes do canal**
  (logotipo grande, nome, servidor e URL de transmissão `.m3u8`).
- Se o jogo não tiver canais, o modal mostra "Nenhum canal informado para este jogo.".
- Os modais fecham com: botão X, botão voltar ou toque fora do painel.

---

## 2) Formato dos campos na API

Os quatro campos têm formatos **diferentes**:

| Campo | Formato | Modelo no SDK |
|---|---|---|
| `canais` | array de **strings** (nomes de canais de TV) | `List<String>` |
| `canais_ia` | array de **strings** (nomes de canais IA) | `List<String>` |
| `canais_simples` | array de **objetos** `{channel_id, channel_name, channel_logo, upvotes, downvotes, updated_at}` | `List<ItemCanalSimples>` |
| `canais_links` | array de **objetos** `{external_channel_id, channel_name, channel_logo, server_id, server_name, server_slug, transmission_url}` | `List<ItemCanalLink>` |

> **A API usa snake_case.** Sem `@SerializedName` o Gson deixaria os campos
> `null`. Por isso `ItemJogos` tem `@SerializedName("canais_simples")`,
> `@SerializedName("canais_links")`, etc.

---

## 3) Fluxo do dado (faixa + modais)

```
API /api/jogos (JSON com "canais", "canais_ia", "canais_simples", "canais_links")
      │  Gson deserializa (ItemJogos + @SerializedName)
      ▼
ItemJogos.java  (List<ItemCanalSimples>, List<ItemCanalLink>, List<String>)
      │  Room salva List<T> como JSON (Converters.java)
      ▼
JogosDatabase (tabela "jogos", versão 3)
      ▼
JogosAdapter.preencherCanais()  →  faixa de chips (apenas canais_links)
      │  chip.setOnClickListener → onCanalClickListener.onCanalClick(jogo, canal)
      │  itemView.setOnClickListener → onItemClickListener.onItemClick(jogo)
      ▼
ActivityEsporte:
      ├─ jogo clicado → CanaisDialogFragment.newInstance(jogo)      (somente canais_links)
      └─ chip clicado → CanalDetalheDialogFragment.newInstance(canal)  (detalhes)
```

---

## 4) Arquivos envolvidos

| Arquivo | Papel |
|---|---|
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemJogos.java` | Modelo do jogo. 4 campos de canais com `@SerializedName` + getters/setters. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemCanalSimples.java` | Objeto de `canais_simples` (`channel_name`, `channel_logo`, votos...). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemCanalLink.java` | Objeto de `canais_links` (`channel_name`, `channel_logo`, `server_name`, `transmission_url`...). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/bancoSql/Converters.java` | Ensina o Room a salvar/ler `List<String>` e `List<ItemCanal*>` como JSON. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/bancoSql/JogosDatabase.java` | Banco. Registra os Converters; versão **3**. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/adapter/JogosAdapter.java` | Faixa de chips. Interfaces `OnItemClickListener` e `OnCanalClickListener`; `preencherCanais()` + `inflarChip()`. |
| `app/src/main/res/layout/api_item_jogos.xml` | Layout da linha do jogo (root vertical + `HorizontalScrollView` com `containerCanaisLinha`). |
| `app/src/main/res/layout/api_item_canal_linha.xml` | Layout de um chip (logo `iv_canal_logo` + nome `tv_canal_nome`). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/dialog/CanaisDialogFragment.java` | Modal com os canais_links (playlist) do jogo (única seção: `Canais:`, via JSON no Bundle). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/dialog/CanalDetalheDialogFragment.java` | Modal de detalhes de um canal link (logo, nome, servidor, URL). |
| `app/src/main/res/layout/dialog_canal_detalhe.xml` | Layout do modal de detalhes do canal. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/ActivityEsporte.java` | Liga os cliques: `setOnItemClickListener` e `setOnCanalClickListener`. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/utils/JogoStatus.java` | Traduz `description` para "Ao Vivo"/"Encerrado"/etc. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/utils/ImageLoader.java` | Carrega logos (URL, base64 ou vazio). |
| `app/src/main/res/drawable/` | `bg_canal_chip_faixa*`, `bg_canal_chip_selector`, `bg_canal_logo_oval`, `bg_canal_chip`, fundos dos modais. |

---

## 5) Como reutilizar em outro lugar

Precisa de um `ItemJogos` para o modal completo e de um `ItemCanalLink` para os
detalhes:

```java
// Modal com os canais_links (playlist) do jogo
CanaisDialogFragment.newInstance(jogo)
        .show(getSupportFragmentManager(), "canais_dialog");

// Modal de detalhes de um canal da faixa
CanalDetalheDialogFragment.newInstance(canal) // canal: ItemCanalLink
        .show(getSupportFragmentManager(), "canal_detalhe");
```

---

## 6) Dicas para mudanças comuns

- **Canais exibidos na faixa da lista:** hoje o `JogosAdapter.preencherCanais()`
  mostra **apenas `canais_links`**. Para incluir `canais_simples`, adicione chips
  no mesmo `containerCanaisLinha`.
- **Tamanho/fonte do chip da faixa:** edite `api_item_canal_linha.xml`
  (`tv_canal_nome` para texto, `iv_canal_logo` para o logo). Fundos em
  `bg_canal_chip_faixa*`.
- **Modal exibe apenas `canais_links` (playlist):** o SDK lista somente os canais
  com `transmission_url` — canais de `canais_simples`/`canais`/`canais_ia` **não**
  aparecem (não têm URL de transmissão e não seriam clicáveis). Edite
  `preencherCanais()`/`adicionarSecaoLinks()` em `CanaisDialogFragment.java`;
  título da seção em `strings.xml` (`modal_canais_sec_links` = "Canais:") e
  texto de vazio em `strings.xml` (`modal_canais_empty`).
- **Cores:** em `app/src/main/res/values/colors.xml` (prefixo `modal_`).
- **Logos via HTTP:** o `AndroidManifest.xml` da biblioteca tem
  `usesCleartextTraffic="true"` (alguns `channel_logo` usam `http://`).
- **Abrir o link de transmissão:** em `CanalDetalheDialogFragment` já há a
  `transmission_url` preenchida; basta adicionar um clique para abrir
  o player/`Intent` externa.

---

## 7) Observações importantes

- **Estrutura diferente por campo:** `canais`/`canais_ia` são listas de nomes;
  `canais_simples`/`canais_links` são listas de objetos (`ItemCanalSimples`/`ItemCanalLink`).
- **snake_case:** qualquer novo campo da API precisa de `@SerializedName`
  com o nome exato do JSON, senão o Gson deixa `null`/não mapeia.
- **Banco:** a versão subiu para **3**. Como usa
  `fallbackToDestructiveMigration()`, o cache antigo é apagado na atualização
  (comportamento normal — é só cache).
- **Compatibilidade:** se os campos não existirem no JSON de um cliente, o Gson
  apenas deixa-os `null`; a faixa é ocultada e o modal mostra
  "Nenhum canal informado". Nada quebra.
- **Somente playlist:** o modal (e a faixa da lista) exibem **apenas** os canais
  de `canais_links`. Sem `canais_links`, o modal mostra "Nenhum canal informado" —
  os índices `canais`, `canais_ia` e `canais_simples` **não** são listados.