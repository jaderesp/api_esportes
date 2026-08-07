# Canais de Transmissão (Modal)

Guia rápido de como funciona e como mexer no **modal de canais de transmissão**
(feature nova no SDK, criada após a API `/api/jogos` passar a devolver os campos
`canais`, `canais_ia`, `canais_simples` e `canais_links`).

---

## 1) O que o usuário vê

- Toque/clique em um **jogo** na lista → abre um **modal (bottom sheet)**.
- O modal mostra:
  - Resumo do jogo: logo do campeonato, horário, status, times e placar.
  - Canais de transmissão organizados em seções:
    `Canais de TV`, `Canais IA`, `Canais Simples`, `Links`.
  - Se o jogo não tiver canais, aparece "Nenhum canal informado para este jogo.".
- O modal fecha com: botão X, botão voltar ou toque fora do painel.

---

## 2) Como o dado chega até o modal (fluxo completo)

```
API /api/jogos (JSON com "canais": [...])
      │  Gson deserializa (campos @SerializedName)
      ▼
ItemJogos.java  (getCanais(), getCanaisIa(), getCanaisSimples(), getCanaisLinks())
      │  Room salva List<String> como JSON (Converters.java)
      ▼
JogosDatabase (tabela "jogos", versão 2)
      │  ActivityEsporte busca do banco
      ▼
JogosAdapter  (setOnItemClickListener → chama a Activity)
      │
      ▼
CanaisDialogFragment.newInstance(jogo).show(...)
      │  monta o layout dialog_canais_jogos.xml
      ▼
Usuário vê os canais
```

---

## 3) Arquivos envolvidos

| Arquivo | Papel |
|---|---|
| `app/src/main/java/com/diegodev/apidesportes/jogos/item/ItemJogos.java` | Modelo. Contém os 4 campos de canais (`List<String>`) + getters/setters. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/bancoSql/Converters.java` | Ensina o Room a salvar `List<String>` como JSON (e ler de volta). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/bancoSql/JogosDatabase.java` | Banco. Registrou os Converters e subiu a versão para **2**. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/dialog/CanaisDialogFragment.java` | O modal em si (monta o conteúdo, estiliza como bottom sheet). |
| `app/src/main/res/layout/dialog_canais_jogos.xml` | Layout do modal (painel inferior, cabeçalho, lista). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/adapter/JogosAdapter.java` | Adiciona o clique no item da lista (callback `OnItemClickListener`). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/ActivityEsporte.java` | Liga o clique do jogo ao modal (`setOnItemClickListener`). |
| `app/src/main/java/com/diegodev/apidesportes/jogos/utils/JogoStatus.java` | Traduz `description` da API para "Ao Vivo"/"Encerrado"/etc. |
| `app/src/main/java/com/diegodev/apidesportes/jogos/utils/ImageLoader.java` | Carrega logos (URL, base64 ou vazio) — usado na lista e no modal. |
| `app/src/main/res/drawable/` | Fundos do modal (painel, chip, handle, botão X) + ícone fechar. |

---

## 4) Como reutilizar em outro lugar

Para abrir o modal de qualquer parte do app, basta ter um `ItemJogos`:

```java
CanaisDialogFragment.newInstance(jogo)
        .show(getSupportFragmentManager(), "canais_dialog");
```

---

## 5) Dicas para mudanças comuns

- **Mudar as seções/ordem dos canais:** edite o método `preencherCanais()`
  em `CanaisDialogFragment.java` e os textos em `strings.xml`
  (`modal_canais_sec_*`).
- **Deixar o chip clicável (ex.: abrir o link do canal):** em `adicionarSecao()`
  já criamos o `TextView` de cada canal com `setFocusable(true)`. Adicione um
  `chip.setOnClickListener(...)` ali. O texto do canal está em `canal`.
- **Ajustar cores:** em `app/src/main/res/values/colors.xml` (prefixo `modal_`).
- **Ajustar altura da lista:** em `dialog_canais_jogos.xml`, no `ScrollView`
  (atributo `android:layout_height`).

---

## 6) Observações importantes

- **Estrutura do campo:** assumimos que `canais`, `canais_ia`, `canais_simples`
  e `canais_links` são arrays de **strings** (nomes de canal). Se a API passar a
  devolver objetos (ex.: `{ "nome": "...", "url": "..." }`), será preciso
  trocar o tipo dos campos em `ItemJogos.java` para uma classe/`@SerializedName`
  com `JsonDeserializer` — o `Converters.java` também mudaria.
- **Banco:** a versão subiu para 2. Como o banco usa
  `fallbackToDestructiveMigration()`, o cache antigo é apagado na atualização
  (comportamento normal — é só cache).
- **Compatibilidade:** se os campos não existirem no JSON de um cliente, o Gson
  apenas deixa-os `null`; o modal mostra "Nenhum canal informado". Nada quebra.
