package com.diegodev.apidesportes.jogos.item;

/**
 * Representa um item da coluna de navegação lateral (recyclerDatas).
 * Pode ser uma data ("dd/MM") ou a opção "Tabela" (classificação), que aparece
 * acima de "HOJE" quando um campeonato está selecionado.
 */
public class DataItem {

    public static final int TIPO_DATA = 1;
    public static final int TIPO_CLASSIFICACAO = 2;

    public final int tipo;

    /** Para TIPO_DATA: "dd/MM". Para TIPO_CLASSIFICACAO: null. */
    public final String valor;

    /** true apenas para a primeira data da lista (que é exibida como "HOJE"). */
    public final boolean primeiroDia;

    private DataItem(int tipo, String valor, boolean primeiroDia) {
        this.tipo = tipo;
        this.valor = valor;
        this.primeiroDia = primeiroDia;
    }

    public static DataItem data(String valor, boolean primeiroDia) {
        return new DataItem(TIPO_DATA, valor, primeiroDia);
    }

    public static DataItem classificacao() {
        return new DataItem(TIPO_CLASSIFICACAO, null, false);
    }
}
