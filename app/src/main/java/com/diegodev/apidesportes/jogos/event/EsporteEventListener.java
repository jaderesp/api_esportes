package com.diegodev.apidesportes.jogos.event;

import androidx.annotation.Nullable;

import com.diegodev.apidesportes.jogos.item.ItemJogos;

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

    @Nullable
    private static EsporteEventCallback listener;

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