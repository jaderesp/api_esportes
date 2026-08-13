# Event Listener — Integração com o app consumidor

Guia completo para o **app que consome o SDK** receber, em tempo real, as
interações do usuário dentro da tela de esportes e **consumir os dados retornados**
para redirecionar/reproduzir no próprio app:

- **Clique na linha de um jogo** (ex.: tocar em "Palmeiras x Flamengo") → evento
  de **jogo** (`onJogoClicado`), com o jogo completo + todos os `canais_links`.
- **Clique em um canal** (ex.: "ESPN HD") dentro do modal de canais → evento de
  **canal** (`aoClicarNoCanal`), com o `stream_id` do canal clicado.

> Disponível a partir da **versão 1.2** do SDK.

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
| `app/src/main/java/com/diegodev/apidesportes/jogos/event/EsporteEventListener.java` | Holder estático do listener + interface `EsporteEventCallback` + `setListener`/`clear`/`notificarJogoClicado`. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/ActivityEsporte.java` | `setList()`: chama `EsporteEventListener.notificarJogoClicado(jogo)` junto com a abertura do modal. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/adapter/JogosAdapter.java` | Dispara `onItemClickListener` no clique da linha (origem do evento). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemJogos.java` / `ItemCanalLink.java` | Modelos entregues no payload. |

---

## 9) Evento de clique no canal — `AoClicarNoCanalListener`

Além do clique na linha do jogo, o SDK também emite um evento **quando o usuário
clica em um canal de transmissão** dentro do modal de canais (seção "Links",
ex.: "Paramount+ 1 FHD").

O evento entrega **somente o `stream_id`** (int) — ID único e fixo do canal —
para o app reproduzir/redirecionar no próprio player.

### Registrar (antes de abrir a tela)

**Java**

```java
EsporteEventListener.definirAoClicarNoCanalListener(idCanal -> {
    // idCanal = stream_id do canal clicado (int)
    abrirPlayerPorStreamId(idCanal); // seu app decide
    return true;  // true = app consumiu o clique
});
```

**Kotlin**

```kotlin
EsporteEventListener.definirAoClicarNoCanalListener { idCanal ->
    abrirPlayerPorStreamId(idCanal) // seu app decide
    true // true = app consumiu o clique
}
```

### Comportamento do retorno

| Retorno do seu callback | O que o SDK faz |
|---|---|
| `true` | O app consumiu o clique — o SDK **não** executa ação padrão. |
| `false` (ou nenhum listener) | O SDK mantém a ação padrão atual (nada além do modal). |

### `stream_id` no payload

O `stream_id` chega como **int** no evento de canal e também está disponível em
cada objeto da lista `getCanaisLinks()` via `ItemCanalLink#getStreamId()`.

> O `stream_id` passou a existir na resposta da API Futebols (`canais_links`) e foi
> mapeado nesta versão do SDK.

---

## 10) Observação sobre versão (JitPack)

Este recurso está publicado na **tag `1.2`** do repositório (`jaderesp/api_esportes`).
O cliente recebe após **subir a versão da dependência**:
`com.github.jaderesp:api_esportes:1.2`. Consultar mais em
[README → Como receber atualizações](../README.md).