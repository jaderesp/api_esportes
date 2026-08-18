package com.diegodev.apidesportes.jogos.utils;

import com.diegodev.apidesportes.R;

/**
 * Traduz o campo "description" vindo da API para textos que o usuário entende.
 * Centralizado aqui para não duplicar a lógica entre a lista (JogosAdapter)
 * e o modal de canais (CanaisDialogFragment).
 */
public final class JogoStatus {

    private JogoStatus() {
    }

    /** Ex.: "Not started" -> "Em breve", "1st half" -> "Ao Vivo". */
    public static String texto(String descricao) {
        if (descricao == null) {
            return "Em breve";
        }
        switch (descricao) {
            case "Not started":
                return "Em breve";
            case "Ended":
                return "Encerrado";
            case "Postponed":
                return "Jogo Adiado";
            case "AP":
                return "Enc. Agregado";
            case "Halftime":
                return "Intervalo";
            case "1st half":
            case "2nd half":
                return "Ao Vivo";
            default:
                return "Em breve";
        }
    }

    /**
     * Fundo (pill) do status, na identidade do widget web:
     * Encerrado (branco), Ao Vivo (verde), Em breve (azul), Adiado (âmbar).
     *
     * @param descricao campo "description" da API.
     * @return drawable do pill correspondente ao status.
     */
    public static int pillRes(String descricao) {
        if (descricao == null) {
            return R.drawable.bg_pill_em_breve;
        }
        switch (descricao) {
            case "Ended":
            case "AP":
                return R.drawable.bg_pill_encerrado;
            case "Halftime":
            case "1st half":
            case "2nd half":
                return R.drawable.bg_pill_ao_vivo;
            case "Postponed":
                return R.drawable.bg_pill_adiado;
            default:
                return R.drawable.bg_pill_em_breve;
        }
    }

    /** Informa se o jogo tem placar para mostrar (ou deve exibir "vs"). */
    public static boolean temPlacar(String descricao) {
        return descricao != null && (descricao.equals("Ended")
                || descricao.equals("AP")
                || descricao.equals("Halftime")
                || descricao.equals("1st half")
                || descricao.equals("2nd half"));
    }
}
