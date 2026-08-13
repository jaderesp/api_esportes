package com.diegodev.apidesportes.jogos.event;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.item.ItemJogos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evento que o SDK emite para o app consumidor acompanhar as interações do
 * usuário dentro da tela de esportes ({@code ActivityEsporte}).
 *
 * <p><b>Modelo de uso (no app que consome o SDK):</b></p>
 * <pre>
 *   // 1) Registre um listener ANTES de abrir a tela de esportes.
 *   EsporteEventListener.setListener(jogo -&gt; {
 *       // 2) O usuário clicou na linha de um jogo. Aqui chegam:
 *       //    - dados do jogo (times, placar, data/hora, campeonato);
 *       //    - todos os canais de transmissão de {@code canais_links}
 *       //      (cada um com channel_name, channel_logo, server_name,
 *       //      transmission_url etc).
 *       List&lt;ItemCanalLink&gt; canais = jogo.getCanaisLinks();
 *       if (canais != null &amp;&amp; !canais.isEmpty()) {
 *           // ex.: tocar o primeiro canal no seu player.
 *       }
 *   });
 *
 *   startActivity(new Intent(context, ActivityEsporte.class));
 * </pre>
 *
 * <p>O callback é chamado <b>na thread principal</b> (UI), no momento do clique
 * na linha de um jogo, ANTES de o modal interno de canais ser exibido.</p>
 *
 * <p><b>Limpeza:</b> use {@link #clear()} (ou {@link #setListener(null)}) quando
 * o app não precisar mais receber eventos, para evitar reter referências.</p>
 *
 * <p><b>Compatibilidade:</b> clientes que NÃO registrarem listener continuam com
 * o comportamento atual (apenas o modal interno abre). Nada quebra.</p>
 *
 * @see #setListener(EsporteEventCallback)
 * @see #clear()
 */
public final class EsporteEventListener {

    /** Callback com o evento de clique em um jogo. */
    public interface EsporteEventCallback {
        /**
         * Chamado quando o usuário clica na linha de um jogo na tela de esportes.
         *
         * @param jogo objeto com todos os dados do jogo clicado, incluindo a
         *             lista {@code canais_links} via {@link ItemJogos#getCanaisLinks()}.
         */
        void onJogoClicado(ItemJogos jogo);
    }

    /**
     * Callback para o clique em um canal de transmissão (seção "Links").
     *
     * <p>Ao clicar em um canal (ex.: "ESPN HD") dentro do modal de canais, o SDK
     * notifica este listener com o {@code stream_id} (ID único e fixo do canal)
     * e aguarda o retorno:</p>
     * <ul>
     *   <li>{@code true} — o app consumiu o clique (o SDK não executa ação padrão);</li>
     *   <li>{@code false} — o app não consumiu (o SDK mantém a ação padrão).</li>
     * </ul>
     */
    public interface AoClicarNoCanalListener {
        /**
         * Chamado quando o usuário clica em um canal de transmissão.
         *
         * @param idCanal ID único e fixo do canal clicado ({@code stream_id}).
         * @return true se o app consumiu o clique.
         */
        boolean aoClicarNoCanal(int idCanal);
    }

    @Nullable
    private static EsporteEventCallback listener;

    @Nullable
    private static AoClicarNoCanalListener canalListener;

    private EsporteEventListener() {
    }

    /**
     * Registra o listener que receberá os eventos de clique do SDK.
     *
     * @param callback callback a ser notificado; passe {@code null} para remover.
     */
    public static void setListener(@Nullable EsporteEventCallback callback) {
        listener = callback;
    }

    /**
     * Remove o listener registrado (equivale a {@code setListener(null)}).
     */
    public static void clear() {
        listener = null;
        canalListener = null;
    }

/**
     * (SDK interno) Registra o listener que receberá os cliques em canais de transmissão.
     *
     * @param listener callback a ser notificado no clique de um canal; passe
     *                 {@code null} para remover.
     */
    public static void onChannelClickListener(@Nullable AoClicarNoCanalListener listener) {
        canalListener = listener;
    }

    /**
     * (SDK interno) Informa se há um listener de canal registrado.
     *
     * <p>O modal de canais usa este método para decidir se deve encerrar a tela
     * do SDK ao clicar em um canal: só encerra quando o app consumidor está
     * ouvindo o evento (e pode assumir a transmissão). Se não houver listener,
     * o clique apenas mantém o modal aberto.</p>
     *
     * @return true se um {@code AoClicarNoCanalListener} foi registrado.
     */
    public static boolean possuiListenerDeCanal() {
        return canalListener != null;
    }

    /**
     * (SDK interno) Notifica o app consumidor sobre o clique em um canal.
     * Chamado pelo modal de canais quando o usuário toca em um canal da seção
     * "Links".
     *
     * <p>Garantias:</p>
     * <ul>
     *   <li>O callback {@code aoClicarNoCanal} é sempre chamado na <b>thread
     *   principal (UI)</b>, mesmo que esta notificação venha de outra thread.</li>
     *   <li>O {@code idCanal} é sempre um {@code int} primitivo, fixo e único
     *   (o {@code stream_id} da API) — nunca a posição na lista nem o nome do
     *   botão. Se o canal não tiver ID, este evento <b>não é disparado</b>
     *   (0 e -1 nunca são enviados).</li>
     * </ul>
     *
     * @param idCanal o {@code stream_id} do canal clicado (nunca 0 ou -1).
     * @return true se o listener registrado consumiu o clique; false caso não
     *         haja listener ou o listener não tenha consumido.
     */
    public static boolean notificarCanalClicado(final int idCanal) {
        final AoClicarNoCanalListener cb = canalListener;
        if (cb == null) {
            return false;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return cb.aoClicarNoCanal(idCanal);
        }
        final boolean[] consumido = {false};
        new Handler(Looper.getMainLooper()).post(() ->
                consumido[0] = cb.aoClicarNoCanal(idCanal));
        return consumido[0];
    }

    /**
     * Monta a tabela {@code stream_id → nome do canal} a partir da lista de
     * canais de um jogo ({@link ItemJogos#getCanaisLinks()}).
     *
     * <p>Como o evento de canal entrega somente o {@code idCanal} (int), use
     * este mapa no app consumidor para traduzir o ID de volta para o nome do
     * canal (ex.: exibir "Paramount+ 1 FHD" ao receber {@code 44043}).</p>
     *
     * @param canais a lista de canais de transmissão do jogo.
     * @return mapa {@code Integer → String} (stream_id → channel_name); chaves
     *         somente para canais com ID válido. Nunca {@code null}.
     */
    public static Map<Integer, String> tabelaIdParaNome(List<ItemCanalLink> canais) {
        Map<Integer, String> tabela = new HashMap<>();
        if (canais != null) {
            for (ItemCanalLink canal : canais) {
                if (canal != null && canal.getStreamId() != null) {
                    tabela.put(canal.getStreamId(), canal.getChannelName());
                }
            }
        }
        return tabela;
    }

    /**
     * (SDK interno) Notifica o app consumidor sobre o clique em um jogo.
     * Chamado pela {@code ActivityEsporte} quando o usuário toca na linha de um
     * jogo. Não faz nada se nenhum listener foi registrado.
     */
    public static void notificarJogoClicado(ItemJogos jogo) {
        EsporteEventCallback cb = listener;
        if (cb != null) {
            cb.onJogoClicado(jogo);
        }
    }
}