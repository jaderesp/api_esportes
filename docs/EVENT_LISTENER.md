# Event Listener — Integração com o app consumidor

Guia completo para o **app que consome o SDK** receber, em tempo real, as
interações do usuário dentro da tela de esportes e **consumir os dados retornados**
para redirecionar/reproduzir no próprio app:

- **Clique na linha de um jogo** (ex.: tocar em "Palmeiras x Flamengo") → evento
  de **jogo** (`onJogoClicado`), com o jogo completo + todos os `canais_links`.
- **Clique em um canal** (ex.: "ESPN HD") dentro do modal de canais → evento de
  **canal** (`aoClicarNoCanal`), com o `stream_id` do canal clicado.

> O evento de canal está disponível a partir da **versão 1.3** do SDK (a versão
> `1.2` ficou com um build antigo em cache no JitPack — use sempre a mais recente, `1.8`).
> O evento de jogo (`onJogoClicado`) existe desde a `1.2`.

---

## 1) O que este recurso faz

O SDK tem uma tela pronta (`ActivityEsporte`) com a lista de jogos. Sem este
recurso, ao clicar em um jogo o SDK abre o **modal interno** de canais e nada
mais é informado ao app de fora.

Com o **Event Listener**, o SDK **continua abrindo o modal interno** (comportamento
atual preservado) e, **no mesmo instante do clique**, também **avisa o seu app**,
entregando o **jogo clicado completo**, incluindo **todos os canais de
`canais_links`** (objetos com nome do canal, logo, servidor e **URL de transmissão
`.m3u8`**).

Em resumo:

- **Disparo:** clique na **linha do jogo** (ex.: Palmeiras x Flamengo).
- **Dado entregue:** o `ItemJogos` clicado com a lista `canais_links` completa.
- **Responsabilidade:** o seu app decide o que fazer com os canais
  (abrir player, listar, redirecionar etc.).

---

## 2) Diagrama do fluxo

```
App consumidor (seu app)
      │  EsporteEventListener.setListener(callback)   ← passo 1 (antes de abrir)
      ▼
ActivityEsporte (tela do SDK)  ← startActivity(...)   ← passo 2
      │
      │   Usuário clica na linha do jogo (ex.: Palmeiras x Flamengo)
      ▼
JogosAdapter.onItemClick(ItemJogos jogo)
      │
      ├──► abre o modal interno CanaisDialogFragment (como sempre)
      │
      └──► EsporteEventListener.notificarJogoClicado(jogo)   ← passo 3
                 │
                 ▼
       seu callback: onJogoClicado(jogo)
                 │  jogo.getCanaisLinks() → List<ItemCanalLink>
                 │  (channel_name, channel_logo, server_name, transmission_url...)
                 ▼
       seu app redireciona/reproduz com os dados recebidos   ← passo 4
```

---

## 3) Como usar (passo a passo)

### Passo 1 — Registrar o listener **antes** de abrir a tela

O SDK guarda o listener de forma estática. Por isso o registro DEVE acontecer
**antes** de chamar `startActivity(...)` para a `ActivityEsporte`.

**Java**

```java
import com.diegodev.apidesportes.jogos.ActivityEsporte;
import com.diegodev.apidesportes.jogos.event.EsporteEventListener;
import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.item.ItemJogos;

// Antes de abrir a tela de esportes:
EsporteEventListener.setListener(jogo -> {
    onJogoClicado(jogo); // seu método
});

startActivity(new Intent(this, ActivityEsporte.class));
```

**Kotlin**

```kotlin
import com.diegodev.apidesportes.jogos.ActivityEsporte
import com.diegodev.apidesportes.jogos.event.EsporteEventListener

EsporteEventListener.setListener { jogo ->
    onJogoClicado(jogo) // seu método
}

startActivity(Intent(this, ActivityEsporte::class.java))
```

### Passo 2 — O clique acontece

O usuário navega na tela de esportes e **clica na linha de um jogo** (ex.:
Palmeiras x Flamengo). O SDK abre o modal interno de canais **e** o seu callback
`onJogoClicado(jogo)` é chamado.

### Passo 3 — Consumir os dados

Dentro do callback, o `jogo` (objeto `ItemJogos`) já traz tudo:

```java
void onJogoClicado(ItemJogos jogo) {
    // Dados do jogo
    String timeCasa     = jogo.getTimeA();      // "Palmeiras"
    String timeFora     = jogo.getTimeB();      // "Flamengo"
    String horario      = jogo.getStart();      // data/hora do jogo
    String campeonato   = jogo.getCampName();   // ex.: "Brasileirão Série A"
    int campId          = jogo.getCampId();
    String descricao    = jogo.getDescription(); // status: ao vivo, encerrado, etc.

    // Canais de transmissão (canais_links) com TODOS os dados
    List<ItemCanalLink> canais = jogo.getCanaisLinks();
    if (canais == null || canais.isEmpty()) {
        // este jogo não tem canal de transmissão
        return;
    }

    // Exemplo: reproduzir o primeiro canal no seu player
    ItemCanalLink primeiro = canais.get(0);
    String url = primeiro.getTransmissionUrl(); // ex.: https://.../teste.m3u8
    String nome = primeiro.getChannelName();    // ex.: "Disney + 1"
    abrirPlayer(url, nome);
}
```

### Passo 4 — Redirecionar/reproduzir

Com a `transmission_url` em mãos, o seu app abre o player, a tela de detalhes ou
qualquer ação própria. O SDK não interfere nessa parte.

---

## 4) Campos disponíveis no payload

### `ItemJogos` (o jogo clicado)

| Método | Conteúdo |
|---|---|
| `getTimeA()` / `getTimeB()` | Nomes dos times (casa/fora). |
| `getLogoA()` / `getLogoB()` | Logos dos times (URL ou base64) ou vazio. |
| `getGolsA()` / `getGolsB()` | Placar atual. |
| `getStart()` | Data/hora do jogo (ISO). |
| `getCampeonato()` / `getCampName()` | Nome do campeonato. |
| `getCampId()` / `getIdCamp()` | ID do campeonato. |
| `getDescription()` | Status da partida (`temaPartida`, "ao vivo", etc.). |
| `getCanais()` | `List<String>` — nomes de canais de TV. |
| `getCanaisIa()` | `List<String>` — nomes de canais de IA. |
| `getCanaisSimples()` | `List<ItemCanalSimples>` — objetos de canais simples. |
| **`getCanaisLinks()`** | **`List<ItemCanalLink>` — canais com URL de transmissão (o foco deste evento).** |

### `ItemCanalLink` (cada canal de `canais_links`)

| Método | Conteúdo |
|---|---|
| `getChannelName()` | Nome do canal (ex.: "Disney + 1"). |
| `getChannelLogo()` | Logotipo do canal (URL ou vazio). |
| `getServerName()` | Nome do servidor. |
| `getServerSlug()` | Slug do servidor. |
| `getExternalChannelId()` | ID externo do canal. |
| `getTransmissionUrl()` | **URL de transmissão (`.m3u8`)** para reproduzir. |

---

## 5) Comportamento e detalhes importantes

- **Thread:** o `onJogoClicado(jogo)` é chamado na **thread principal (UI)**,
  no momento do clique. Você pode atualizar a sua UI diretamente.
- **Modal interno preservado:** o SDK continua abrindo o modal de canais no clique.
  Se não quiser que ele abra, é preciso combinar uma opção/nova versão do SDK
  (hoje ele sempre abre).
- **Sem quebrar clientes atuais:** se nenhum listener for registrado, o SDK se
  comporta exatamente como antes (o modal interno abre e nada é emitido).
- **Jogo sem canais:** `getCanaisLinks()` retorna `null` ou lista vazia. Trate
  esse caso no seu app (o SDK mostra "Nenhum canal informado" no modal).
- **Campos da API em snake_case:** o payload usa os mesmos nomes do JSON
  (`canais_links`, `channel_name`, `transmission_url`...). O Gson do SDK já mapeia
  tudo — no seu app você só usa os getters.

---

## 6) Limpeza (evitar vazamento de referência)

O listener é **estático**: se você registrou um objeto ligado a uma Activity
(ex.: o próprio `this` da Activity), remova-o quando não precisar mais, para não
segurar a Activity em memória:

```java
// Em onDestroy / onStop / quando sair da tela que registrou:
EsporteEventListener.clear();              // remove
// ou
EsporteEventListener.setListener(null);    // também remove
```

Recomendação: registre o listener na Activity que inicia o SDK e chame
`EsporteEventListener.clear()` no `onDestroy()` dela.

---

## 7) Dicas e erros comuns

| Situação | Como resolver |
|---|---|
| Nada é chamado no clique. | Verifique se `setListener(...)` foi chamado **antes** do `startActivity` da `ActivityEsporte`. |
| Tela aberta, callback nunca dispara. | Confirme que o app subiu para a **versão 1.2+** da dependência e fez `Sync`/rebuild. |
| `getCanaisLinks()` vazio. | Jogo sem canal de transmissão — normal. Trate como "sem canal". |
| Quer a URL direto na linha. | Use `canais.get(0).getTransmissionUrl()` no callback (evento por canal em chip é uma evolução futura). |

---

## 8) Arquivos envolvidos (para manutenção do SDK)

| Arquivo | Papel |
|---|---|
| `app/src/main/java/com/diegodev/apidesportes/jogos/event/EsporteEventListener.java` | Holder estático dos listeners (`EsporteEventCallback` + `AoClicarNoCanalListener`), `setListener`/`onChannelClickListener`/`clear`, `notificarJogoClicado`/`notificarCanalClicado`, `possuiListenerDeCanal()` e helper `tabelaIdParaNome`. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/ActivityEsporte.java` | `setList()`: chama `EsporteEventListener.notificarJogoClicado(jogo)` junto com a abertura do modal. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/adapter/JogosAdapter.java` | Dispara `onItemClickListener` no clique da linha (origem do evento). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/dialog/CanaisDialogFragment.java` | Chips da seção "Canais:" notificam `notificarCanalClicado(stream_id)` no clique (só com ID válido) e encerram o SDK via `requireActivity().finish()` quando há listener de canal. Chips com logotipo (`channel_logo`) e `bg_canal_chip_selector_modal` (foco TV). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemJogos.java` / `ItemCanalLink.java` | Modelos entregues no payload (`ItemCanalLink` tem `getStreamId()`). |

---

## 9) Evento de clique no canal — `AoClicarNoCanalListener`

Além do clique na linha do jogo, o SDK também emite um evento **quando o usuário
clica em um canal de transmissão** dentro do modal de canais (seção "Canais:",
ex.: "Paramount+ 1 FHD").

O evento entrega **somente o `stream_id`** (int) — ID único e fixo do canal —
para o app reproduzir/redirecionar no próprio player.

### Registrar (antes de abrir a tela)

**Java**

```java
EsporteEventListener.onChannelClickListener(idCanal -> {
    // idCanal = stream_id do canal clicado (int)
    abrirPlayerPorStreamId(idCanal); // seu app decide
    return true;  // true = app consumiu o clique
});
```

**Kotlin**

```kotlin
EsporteEventListener.onChannelClickListener { idCanal ->
    abrirPlayerPorStreamId(idCanal) // seu app decide
    true // true = app consumiu o clique
}
```

### Comportamento do retorno

| Retorno do seu callback | O que o SDK faz |
|---|---|
| `true` | O app consumiu o clique — o SDK **encerra a tela** (via `finish()`) para o seu app assumir a transmissão. |
| `false` | O app **não** consumiu o clique — o SDK **também encerra a tela**. |
| Nenhum listener | O SDK mantém o comportamento anterior: apenas o modal continua aberto, nada é encerrado. |

> **Importante (pedido do cliente):** assim que o usuário clica em um canal da seção
> "Canais:", o SDK notifica o seu listener **e encerra a tela do SDK**
> (`ActivityEsporte.finish()`), desde que haja um listener de canal registrado.
> Assim o seu app recebe o `stream_id` e assume a reprodução sem voltar para a tela
> de esportes. Sem listener registrado, o modal só continua aberto (nada quebra).
> O chip do canal agora também exibe o **logotipo** (`channel_logo`) quando a API o
> informa, e tem **feedback de foco** para navegação com controle remoto (TV).

### Regras do `idCanal` (importante)

- **Tipo:** `int` **primitivo**, **fixo** e **único** por canal (é o `stream_id` da
  API Futebols).
- **Nunca** é a posição na lista nem o nome do botão ("ESPN HD²" muda; o ID não).
- **Sem ID → sem evento:** se o canal não tiver `stream_id`, o SDK **não dispara**
  o evento — `0` e `-1` **nunca** são enviados. Trate o "sem canal" silenciosamente.
- **Thread:** o callback `aoClicarNoCanal` chega **sempre na UI thread (principal)**.
- **Fixo no tempo:** o `stream_id` é estável por canal (o nome pode mudar, o ID não).

### Tabela ID → nome do canal

O evento entrega somente o `idCanal` (int). Para traduzir de volta para o **nome do
canal**, use o helper do SDK:

**Java**

```java
// No evento de jogo (onJogoClicado) ou onde você tiver a lista de canais:
Map<Integer, String> tabela = EsporteEventListener.tabelaIdParaNome(jogo.getCanaisLinks());
// tabela.get(idCanal)  → ex.: 44043 → "Paramount+ 1 FHD"

// No callback de canal:
EsporteEventListener.onChannelClickListener(idCanal -> {
    String nome = tabela.get(idCanal);   // nome do canal correspondente
    abrirPlayerPorStreamId(idCanal, nome);
    return true;
});
```

**Kotlin**

```kotlin
val tabela = EsporteEventListener.tabelaIdParaNome(jogo.canaisLinks)
EsporteEventListener.onChannelClickListener { idCanal ->
    val nome = tabela[idCanal]
    abrirPlayerPorStreamId(idCanal, nome)
    true
}
```

> O retorno é um `Map<Integer, String>` (stream_id → channel_name) montado a
> partir da lista de `canais_links` do jogo. Chaves apenas para canais com ID.

### Encerramento do SDK ao clicar (pedido do cliente)

Ao clicar em um canal da seção "Canais:" **com `stream_id` válido**, o SDK:

1. Notifica o seu listener (`aoClicarNoCanal(idCanal)`) — sempre na UI thread;
2. Encerra a tela do SDK com `requireActivity().finish()`, **desde que exista um
   listener de canal registrado** (`EsporteEventListener.possuiListenerDeCanal()`),
   para o seu app assumir a transmissão sem o usuário voltar à tela de esportes.

Isso vale **independentemente** do retorno do callback (`true` ou `false`): o que
decide o encerramento é a **existência** do listener, não o retorno. Clientes que
não registrarem o listener mantêm o modal aberto, sem encerrar nada.

### Logotipo e foco nos chips

- O chip de cada canal da seção "Canais:" agora exibe o **`channel_logo`** (URL ou
  base64) quando a API informa o campo; sem logo, mostra apenas o nome.
- Os chips do modal (seção `Canais:`, canais_links) usam o selector
  `bg_canal_chip_selector_modal`, que **destaca o chip quando focado** — amigável
  para navegação com controle remoto em Android TV.

### `stream_id` no payload

O `stream_id` está disponível em cada objeto da lista `getCanaisLinks()` via
`ItemCanalLink#getStreamId()` (retorna `Integer`; `null` se a API não informou).
O evento de canal só é disparado quando esse valor é não-nulo.

> O `stream_id` passou a existir na resposta da API Futebols (`canais_links`) e foi
> mapeado nesta versão do SDK. A API também devolve `numeric_channel_id` e
> `collected_at`, ainda não mapeados.

---

## 10) Observação sobre versão (JitPack)

Os eventos de jogo e de canal estão publicados no repositório
(`jaderesp/api_esportes`). Use a versão **`1.8`** (mais recente):
`com.github.jaderesp:api_esportes:1.8`.

> **Atenção à `1.2`:** essa versão ficou com um build antigo em cache no JitPack
> (os métodos novos não apareceram após limpar o cache local — o cache é no
> servidor do JitPack). Sempre use a versão mais recente publicada e consulte
> [README → Como receber atualizações](../README.md).