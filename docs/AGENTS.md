# AGENTS.md — Contexto do Projeto

Arquivo de contexto para dar continuidade ao desenvolvimento da próxima sessão.
Leia este arquivo antes de iniciar qualquer trabalho neste repositório.

## O que é o projeto

**SDK de Esportes - Futebols** — biblioteca Android (Java/Kotlin) para apps Android/Android TV.
Integra dados de jogos, campeonatos e placares da API **Futebols**:

- Base API: `https://api.futebols.com.br/api/`
- Rotas: `GET /api/campeonatos`, `GET /api/jogos` e `GET /api/campeonato/{id}/classificacao`
- Autenticação: header `Authorization: Bearer TOKEN`
- Distribuição via **JitPack**: `com.github.jaderesp:api_esportes:<versao>` (repo atual: `jaderesp/api_esportes`, o antigo `DevXoneTv` está obsoleto)
- Suporte: https://futebols.com.br/ | Docs: Postman (link no README.md)

## Estrutura

- `app/` — módulo de **biblioteca** (`com.android.library`, namespace `com.diegodev.apidesportes`)
  - `jogos/ActivityEsporte.java` — tela principal (fullscreen, paisagem, imersiva)
  - `jogos/response/` — chamadas de API (Retrofit/OkHttp) e parsing
  - `jogos/interfac/` — interfaces Retrofit (`ServiceJogos`, `ServiceCate`)
  - `jogos/bancoSql/` — Room/SQLite (cache local: `JogosDatabase`, `CategoriaDatabase`)
  - `jogos/adapter/`, `jogos/item/`, `jogos/callback/`, `jogos/dialog/`, `jogos/utils/`
  - `jogos/callback/na.java` + `dja.java` — decriptação nativa da URL base (AES/CBC + JNI)
- `jogos/dialog/CanaisDialogFragment.java` — modal (bottom sheet) com **todas** as seções de canais de transmissão (Links, Simples, TV, IA); aberto ao clicar na linha do jogo (ver `docs/CANAIS_TRANSMISSAO.md`)
- `jogos/dialog/CanalDetalheDialogFragment.java` — modal (bottom sheet) com os detalhes de um canal link (logo, nome, servidor, transmission_url); aberto ao clicar em um chip da faixa da linha do jogo
- `jogos/event/EsporteEventListener.java` — **evento para o app consumidor**: holder estático + interface `EsporteEventCallback` (`onJogoClicado(ItemJogos)`); disparado no clique da linha do jogo em `ActivityEsporte.setList()` junto com a abertura do modal; registro via `setListener(...)` antes do `startActivity`; limpeza com `clear()` (ver `docs/EVENT_LISTENER.md`)
- `jogos/item/ItemCanalSimples.java` + `jogos/item/ItemCanalLink.java` — objetos de `canais_simples`/`canais_links` da API (não são mais listas de strings)
- `jogos/item/ItemClassificacao.java` + `jogos/response/ApiClassificacaoCaller.java` + `jogos/bancoSql/ClassificacaoDatabase.java` — tabela de classificação (`GET /api/campeonato/{id}/classificacao`); opção "Tabela" aparece acima de "HOJE" ao selecionar um campeonato (ver `docs/CLASSIFICACAO.md`)
  - `cpp/api_esportes.cpp` — proteção/ofuscação da URL (JNI); `verificarUrlNativa` faz `exit(0)` se o host não for o esperado
  - `jniLibs/` — `.so` **pré-compilados** por ABI (arm64-v8a, armeabi-v7a, x86, x86_64)
  - `res/layout/` — `frame_esportes.xml` (tela principal), `api_category`, `api_item_jogos`, `api_item_canal_linha` (chip da faixa), `api_item_data`, `api_item_camp`, `api_expired`
  - `res/raw/` — animações Lottie (`.json`)
- `app/src/main/AndroidManifest.xml` — biblioteca com `usesCleartextTraffic="true"` (logotipos de canais usam `http://`)
- `demo/` — módulo de **aplicação** criado para teste em aparelho real (instalável)
  - `MainActivity` salva token em `SharedPreferences` `ApiEsporteBrPrefs` (chave `token`) e abre `ActivityEsporte`
- `docs/` — documentação e este arquivo

## Configuração de URL base (prioridade)

1. `API_BASE_URL` do build (`gradle.properties` ou variável de ambiente) — hoje definido em `gradle.properties`
2. URL nativa/ofuscada (JNI `na.ae()` + `dja.dpt`)
3. Fallback interno: `https://api.futebols.com.br/api/`

Lógica em `app/src/main/java/com/diegodev/apidesportes/jogos/utils/ApiConfig.java`.

## Como abrir a tela no app consumidor

- Token: `SharedPreferences` arquivo `ApiEsporteBrPrefs`, chave `token` (se vazio → `finish()`)
- Activity: `com.diegodev.apidesportes.jogos.ActivityEsporte` (declarada `exported=true` no manifest)

## Build e teste

- `local.properties` foi criado localmente (não versionar): `sdk.dir=C\:\\Users\\jmsof\\AppData\\Local\\Android\\Sdk`
- Build da biblioteca: `.\gradlew.bat :app:assembleDebug`
- Build do app demo: `.\gradlew.bat :demo:assembleDebug`
- **Token de testes do demo (NUNCA versionar no repositório):** lido no build pela variável de ambiente `FUTEBOLS_TOKEN` e injetado no `BuildConfig` do demo (`demo/build.gradle.kts`). No Windows:
  `$env:FUTEBOLS_TOKEN="SEU_TOKEN"; .\gradlew.bat :demo:assembleDebug`
  - Com a variável vazia/sem ela, o campo de token abre em branco no app demo e o token pode ser colado manualmente.
  - Segredo/senha e afins também não devem ser commitados.
- Instalar no celular (adb não está no PATH — usar caminho completo):
  `& "C:\Users\jmsof\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r demo\build\outputs\apk\debug\demo-debug.apk`
- Abrir: `adb shell am start -n com.diegodev.apidesportes.demo/.MainActivity`
- Emulador disponível: AVD `Medium_Phone_API_36.1`
- Celular usado nos testes: Samsung `SM_G990E` (adb serial `RQCW504BWSK`)
- **Android TV usada nos testes da faixa de canais:** `192.168.3.14:5555` (sempre usar `-s 192.168.3.14:5555`; há outro device `RQCW504BWSK` **unauthorized** — nunca instalar sem `-s`):
  - Instalar: `& "C:\Users\jmsof\AppData\Local\Android\Sdk\platform-tools\adb.exe" -s 192.168.3.14:5555 install -r demo\build\outputs\apk\debug\demo-debug.apk`
  - Abrir: `adb -s 192.168.3.14:5555 shell am start -n com.diegodev.apidesportes.demo/.MainActivity`
  - Verificar processo: `adb -s 192.168.3.14:5555 shell "pidof com.diegodev.apidesportes.demo"`

## Mudanças de layout/source e atualização dos clientes

- Layout/Java/XML são compilados pelo JitPack a partir do repo; cliente só recebe mudança após: **push + tag nova → cliente subir versão da dependência → cliente publicar o app**.
- **IMPORTANTE (nativo):** o `app/build.gradle` **não** tem `externalNativeBuild` configurado. As `.so` em `jniLibs/` são pré-compiladas e comitadas. Mudanças em `cpp/api_esportes.cpp` (ou `na.java`/`dja.java`) **não terão efeito** nos clientes a menos que: (a) as `.so` sejam recompiladas por ABI e comitadas, ou (b) `externalNativeBuild` seja configurado no build do `app`. Pendência aberta nesta sessão.

## Estado atual (última sessão)

- **Implementado o Event Listener** (solicitação de cliente que consome o SDK via importação; decisões: clique na **linha do jogo** entrega **todos** os `canais_links`; manter o modal interno; entrega via **callback estático**):
  - Novo `jogos/event/EsporteEventListener.java` — `setListener(EsporteEventCallback)` / `clear()` / `notificarJogoClicado(ItemJogos)`. Callback na thread principal.
  - `ActivityEsporte.setList()` — `setOnItemClickListener` agora também chama `EsporteEventListener.notificarJogoClicado(jogo)` após abrir o modal (clientes sem listener mantêm o comportamento antigo).
  - Documentação completa para o cliente: `docs/EVENT_LISTENER.md` + seção no `README.md` (Passo 4) + entrada em Novidades por versão (na `1.2`).
  - **Publicado dentro da tag `1.2`** (não existe tag `1.3` — solicitado manter versão `1.2`; o recurso saiu junto na `1.2` enviada via JitPack).
- **Adicionado o Event Listener de canal (solicitação do programador do cliente):** dispara no **clique de um canal na seção "Links" dentro do modal** `CanaisDialogFragment`; entrega **somente o `stream_id` (int)**:
  - `ItemCanalLink` ganhou o campo `stream_id` com `@SerializedName("stream_id")` + `getStreamId()`/`setStreamId()` — declarado como **`Integer`** (nullable): se a API não mandar o campo, `getStreamId()` retorna `null` e o evento **não é disparado** (nunca envia `0`/`-1`). Campo confirmado na API real: `stream_id` = int, ex.: `44043`; a API também devolve `numeric_channel_id` e `collected_at` (ainda não mapeados).
  - `EsporteEventListener` ganhou a interface `AoClicarNoCanalListener` (`boolean aoClicarNoCanal(int idCanal)`) + registrador `definirAoClicarNoCanalListener(@Nullable ...)` + `notificarCanalClicado(int)` e `clear()` limpa os dois listeners.
  - **Regras atendidas (pedido do cliente):** `idCanal` é `int` **primitivo**, **fixo e único** por canal (é o `stream_id`, nunca posição na lista nem nome do botão); **sem ID → não dispara**; callback **sempre na UI thread** (o `notificarCanalClicado` garante via `Handler(Looper.getMainLooper())` quando chamado fora da main); helper público `tabelaIdParaNome(List<ItemCanalLink>)` → `Map<Integer,String>` (stream_id → channel_name) para o app traduzir o ID de volta ao nome.
  - `CanaisDialogFragment.adicionarSecaoLinks()` — cada chip de Link tem `setOnClickListener` → só notifica `EsporteEventListener.notificarCanalClicado(stream_id)` se `getStreamId() != null`.
  - **Permanece dentro da tag `1.2`** (mesma versão publicada).
- Criado módulo `demo/` (app host para teste):
  - `settings.gradle.kts` — adicionado `include(":demo")`
  - `demo/build.gradle.kts`, manifest, `MainActivity.java`, layout, strings, `demo/.gitignore` (`/build`)
  - `local.properties` (não versionar)
- Build e instalação do demo no celular com sucesso.
- Teste manual: abrir `Demo Esportes` → colar token Futebols → "Abrir Esportes".
- **Release 1.2 publicada** (branch `feature/classificacao-canais`, tag `1.2`): canais de transmissão + tabela de classificação + módulo demo.
- **Implementada tabela de classificação** (nova rota `api/campeonato/{id}/classificacao`):
  - Ao selecionar um campeonato, aparece a opção **"Tabela"** acima de "HOJE" na coluna lateral (`DataItem`/`DataAdapter`); ao clicar, mostra a classificação no espaço central (`ClassificacaoAdapter`).
  - Novos arquivos: `ItemClassificacao`, `ServiceClassificacao`, `ApiClassificacaoCaller` (aceita array direto ou objeto com array), `ClassificacaoDao`, `ClassificacaoDatabase`, `DataItem`, layouts `api_classificacao_header`, `api_item_classificacao`, `api_item_classificacao_opcao`.
  - Modelo mapeia padrão snake_case com alternativas camelCase; ajustar `@SerializedName` se o JSON real diferir (ver `docs/CLASSIFICACAO.md`).
  - **JSON real (camp 1957, Brasileiro Série A):** array de objetos com `time_nome` no nível superior (snake_case). A API **não retorna logo/escudo** — placeholder `ic_time_placeholder` (escudo genérico) no `ClassificacaoAdapter` via `ImageLoader.load(..., placeholderRes)`.
  - **Corrida de leitura corrigida:** `buscarClassificacao` chama `limparPorCamp(campId)` **antes** de disparar a busca + polling, para a tela não exibir linhas antigas (ex.: sem nome) do cache Room.
  - **Fontes da tabela reajustadas:** cabeçalho/colunas `_7sdp`, posição `_8sdp`, altura da linha `_32sdp`.
- **Implementados os canais de transmissão** (modal + faixa na lista):
  - `ItemJogos` tem os campos `canais`, `canais_ia` (`List<String>`) e `canais_simples`, `canais_links` (`List<ItemCanalSimples>`/`List<ItemCanalLink>` — **objetos**, não strings).
  - **Causa raiz corrigida:** a API usa **snake_case** (`canais_links`, `canais_simples`, `canais_ia`, `canais`); sem `@SerializedName` o Gson deixava os campos `null` (nada aparecia). Adicionados os `@SerializedName` em `ItemJogos` e nos novos models.
  - `JogosDatabase` versão 1 → **3** + `Converters.java` (Room salva `List<T>` como JSON; usa `fallbackToDestructiveMigration`, cache antigo é apagado).
  - **Faixa de chips na linha do jogo** (apenas `canais_links`): `JogosAdapter.preencherCanais()` + `inflarChip()`; layout `api_item_canal_linha.xml` (logo `iv_canal_logo` + nome `tv_canal_nome`) dentro de `HorizontalScrollView` em `api_item_jogos.xml`. Interfaces `OnItemClickListener` (jogo) e `OnCanalClickListener` (chip).
  - **CanalDetalheDialogFragment** + `dialog_canal_detalhe.xml`: modal de detalhes do canal (logo, nome, servidor, transmission_url).
  - `CanaisDialogFragment`: reordenado para exibir a seção `Links` **primeiro** (objetos via `ARG_CANAIS_LINKS_JSON`, Gson `TypeToken<List<ItemCanalLink>>`), depois Simples → TV → IA; vazio → "Nenhum canal informado".
  - `AndroidManifest.xml` da biblioteca: `usesCleartextTraffic="true"` (logos de canal em `http://`).
  - Design da faixa: `bg_canal_chip_faixa*`, `bg_canal_chip_selector`, `bg_canal_logo_oval` (drawables). Fonte do nome do canal atualmente `_7sdp` (aumentada 250% sobre `_2sdp`, decisão do usuário).
  - Novo utils `JogoStatus` (status/placar) e `ImageLoader` (logos URL/base64).
  - Testado na **Android TV `192.168.3.14`**: Build (`.\gradlew.bat :demo:assembleDebug --console=plain -q`), install (`adb -s 192.168.3.14:5555 install -r ...`), e verificação no Room da TV (`run-as com.diegodev.apidesportes.demo cat databases/jogos.db`): 232 jogos; **1** com `canais_links` real (jogo `229020` Goiás x Londrina, canal "Disney + 1", transmission_url `https://api.systemupdate.vip/live/teste11/5555z/78956.m3u8`, start `2026-08-10T22:30:00.000Z`); 46 com `canais_simples` reais; `[]` demais.
  - **Documentação detalhada:** `docs/CANAIS_TRANSMISSAO.md`.
- Pendente decisão do usuário: configurar `externalNativeBuild` do CMake.

## Observações técnicas

- `ActivityEsporte` usa `salvarHoraRedeSaoPaulo` (SharedPreferences `ClienteSetup`, chave `DataAtual`) e gera 5 datas a partir da hora do servidor (America/Sao_Paulo).
- Room: bancos `JogosDatabase`/`CategoriaDatabase`/`ClassificacaoDatabase` (cache local; `classificacao.db` com WAL). Aviso de schema export não configurado (cosmético).
- Dependências principais: Retrofit 2.9.0, OkHttp 4.9.3, Gson, Glide 4.16.0, Room 2.6.1, Lottie 6.1.0, sdp 1.1.1.
- minSdk 21, compileSdk 35, Java 11.
