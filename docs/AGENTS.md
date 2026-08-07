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
- `jogos/dialog/CanaisDialogFragment.java` — modal (bottom sheet) de canais de transmissão; aberto ao clicar num jogo (ver `docs/CANAIS_TRANSMISSAO.md`)
- `jogos/item/ItemClassificacao.java` + `jogos/response/ApiClassificacaoCaller.java` + `jogos/bancoSql/ClassificacaoDatabase.java` — tabela de classificação (`GET /api/campeonato/{id}/classificacao`); opção "Tabela" aparece acima de "HOJE" ao selecionar um campeonato (ver `docs/CLASSIFICACAO.md`)
  - `cpp/api_esportes.cpp` — proteção/ofuscação da URL (JNI); `verificarUrlNativa` faz `exit(0)` se o host não for o esperado
  - `jniLibs/` — `.so` **pré-compilados** por ABI (arm64-v8a, armeabi-v7a, x86, x86_64)
  - `res/layout/` — `frame_esportes.xml` (tela principal), `api_category`, `api_item_jogos`, `api_item_data`, `api_item_camp`, `api_expired`
  - `res/raw/` — animações Lottie (`.json`)
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
- Instalar no celular (adb não está no PATH — usar caminho completo):
  `& "C:\Users\jmsof\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r demo\build\outputs\apk\debug\demo-debug.apk`
- Abrir: `adb shell am start -n com.diegodev.apidesportes.demo/.MainActivity`
- Emulador disponível: AVD `Medium_Phone_API_36.1`
- Celular usado nos testes: Samsung `SM_G990E` (adb serial `RQCW504BWSK`)

## Mudanças de layout/source e atualização dos clientes

- Layout/Java/XML são compilados pelo JitPack a partir do repo; cliente só recebe mudança após: **push + tag nova → cliente subir versão da dependência → cliente publicar o app**.
- **IMPORTANTE (nativo):** o `app/build.gradle` **não** tem `externalNativeBuild` configurado. As `.so` em `jniLibs/` são pré-compiladas e comitadas. Mudanças em `cpp/api_esportes.cpp` (ou `na.java`/`dja.java`) **não terão efeito** nos clientes a menos que: (a) as `.so` sejam recompiladas por ABI e comitadas, ou (b) `externalNativeBuild` seja configurado no build do `app`. Pendência aberta nesta sessão.

## Estado atual (última sessão)

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
- **Implementado modal de canais de transmissão** (clique no jogo → bottom sheet):
  - `ItemJogos` ganhou os campos `canais`, `canais_ia`, `canais_simples`, `canais_links` (`List<String>`).
  - `JogosDatabase` versão 1 → **2** + `Converters.java` (Room salva `List<String>` como JSON; usa `fallbackToDestructiveMigration`, cache antigo é apagado).
  - `CanaisDialogFragment` + layout `dialog_canais_jogos.xml`.
  - `JogosAdapter` com `setOnItemClickListener`; `ActivityEsporte` abre o modal.
  - Novos utils compartilhados: `JogoStatus` (status/placar) e `ImageLoader` (logos URL/base64).
  - **Documentação detalhada:** `docs/CANAIS_TRANSMISSAO.md`.
  - Obs.: campos modelados como arrays de strings; se a API mudar para objetos, ajustar `ItemJogos`/`Converters` (ver doc).
- Pendente decisão do usuário: configurar `externalNativeBuild` do CMake.

## Observações técnicas

- `ActivityEsporte` usa `salvarHoraRedeSaoPaulo` (SharedPreferences `ClienteSetup`, chave `DataAtual`) e gera 5 datas a partir da hora do servidor (America/Sao_Paulo).
- Room: bancos `JogosDatabase`/`CategoriaDatabase`/`ClassificacaoDatabase` (cache local; `classificacao.db` com WAL). Aviso de schema export não configurado (cosmético).
- Dependências principais: Retrofit 2.9.0, OkHttp 4.9.3, Gson, Glide 4.16.0, Room 2.6.1, Lottie 6.1.0, sdp 1.1.1.
- minSdk 21, compileSdk 35, Java 11.
