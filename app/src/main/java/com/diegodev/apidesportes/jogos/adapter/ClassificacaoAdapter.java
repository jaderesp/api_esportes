package com.diegodev.apidesportes.jogos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.item.ItemClassificacao;
import com.diegodev.apidesportes.jogos.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabela de classificação. A posição 0 do adapter é o cabeçalho fixo com os
 * nomes das colunas (#, Time, J, V, E, D, Pts); as demais são linhas de times.
 *
 * Times que pertencem a uma mesma zona de promoção (dados da API: promocao,
 * promocao_slug e promocao_cor) são agrupados sob uma barra retangular colorida
 * com o nome da promoção (ex.: "Copa Libertadores"), e cada linha do grupo ganha
 * uma faixa lateral na mesma cor — dando um entendimento rápido ao usuário.
 */
public class ClassificacaoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HEADER = 0;
    private static final int VIEW_ROW = 1;
    private static final int VIEW_PROMOCAO = 2;

    private final Context context;
    private final List<ItemClassificacao> list;
    private final List<Object> itens; // ItemClassificacao ou PromocaoItem

    public ClassificacaoAdapter(Context context, List<ItemClassificacao> list) {
        this.context = context;
        this.list = list;
        this.itens = agruparPorPromocao(list);
    }

    /** Insere uma barra (PromocaoItem) antes de cada grupo de times da mesma promoção. */
    private List<Object> agruparPorPromocao(List<ItemClassificacao> lista) {
        List<Object> resultado = new ArrayList<>();
        String slugAtual = null;
        for (ItemClassificacao item : lista) {
            String slug = item.getPromocaoSlug();
            boolean temPromocao = !TextUtils.isEmpty(slug) && !TextUtils.isEmpty(item.getPromocao());
            if (temPromocao && !slug.equals(slugAtual)) {
                resultado.add(new PromocaoItem(item.getPromocao(), item.getPromocaoCor()));
                slugAtual = slug;
            } else if (!temPromocao) {
                slugAtual = null;
            }
            resultado.add(item);
        }
        return resultado;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HEADER;
        }
        Object item = itens.get(position - 1);
        return item instanceof PromocaoItem ? VIEW_PROMOCAO : VIEW_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.api_classificacao_header, parent, false));
        }
        if (viewType == VIEW_PROMOCAO) {
            return new PromocaoHolder(inflater.inflate(R.layout.api_item_classificacao_promocao, parent, false));
        }
        return new RowHolder(inflater.inflate(R.layout.api_item_classificacao, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PromocaoHolder) {
            PromocaoHolder promo = (PromocaoHolder) holder;
            PromocaoItem item = (PromocaoItem) itens.get(position - 1);
            promo.tvPromocaoNome.setText(item.nome);
            promo.containerPromocao.setBackgroundColor(parseColor(item.cor));
        } else if (holder instanceof RowHolder) {
            RowHolder row = (RowHolder) holder;
            ItemClassificacao item = (ItemClassificacao) itens.get(position - 1);

            row.tvPosicao.setText(String.valueOf(item.getPosicao()));
            row.tvJogos.setText(String.valueOf(item.getJogos()));
            row.tvVitorias.setText(String.valueOf(item.getVitorias()));
            row.tvEmpates.setText(String.valueOf(item.getEmpates()));
            row.tvDerrotas.setText(String.valueOf(item.getDerrotas()));
            row.tvPontos.setText(String.valueOf(item.getPontos()));
            row.tvTimeName.setText(item.getTimeName());

            // Faixa lateral colorida na cor da promoção (bloco contínuo do grupo).
            row.vPromoStripe.setBackgroundColor(parseColor(item.getPromocaoCor()));

            // A API envia time_logo (URL); se vier vazio, usa escudo genérico como placeholder.
            ImageLoader.load(context, item.getLogo(), row.ivTimeLogo, R.drawable.ic_time_placeholder);
        }
    }

    /** Converte "#RRGGBB" em cor; se inválido/vazio, usa transparente (sem destaque). */
    private int parseColor(String hex) {
        if (TextUtils.isEmpty(hex)) {
            return Color.TRANSPARENT;
        }
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return Color.TRANSPARENT;
        }
    }

    @Override
    public int getItemCount() {
        return itens.size() + 1; // +1 do cabeçalho
    }

    /** true se a posição (do adaptador) é uma linha de time focável.
     *  Cabeçalho fixo (posição 0) e barras de zona de promoção não recebem foco. */
    public boolean isFocavel(int position) {
        if (position == 0) {
            return false;
        }
        return !(itens.get(position - 1) instanceof PromocaoItem);
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class PromocaoHolder extends RecyclerView.ViewHolder {
        View containerPromocao;
        TextView tvPromocaoNome;

        PromocaoHolder(@NonNull View itemView) {
            super(itemView);
            containerPromocao = itemView.findViewById(R.id.containerPromocao);
            tvPromocaoNome = itemView.findViewById(R.id.tvPromocaoNome);
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView tvPosicao, tvTimeName, tvJogos, tvVitorias, tvEmpates, tvDerrotas, tvPontos;
        ImageView ivTimeLogo;
        View vPromoStripe;

        RowHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicao = itemView.findViewById(R.id.tvPosicao);
            tvTimeName = itemView.findViewById(R.id.tvTimeName);
            tvJogos = itemView.findViewById(R.id.tvJogos);
            tvVitorias = itemView.findViewById(R.id.tvVitorias);
            tvEmpates = itemView.findViewById(R.id.tvEmpates);
            tvDerrotas = itemView.findViewById(R.id.tvDerrotas);
            tvPontos = itemView.findViewById(R.id.tvPontos);
            ivTimeLogo = itemView.findViewById(R.id.ivTimeLogo);
            vPromoStripe = itemView.findViewById(R.id.vPromoStripe);
        }
    }

    /** Item de agrupamento: barra colorida com o nome da promoção. */
    static class PromocaoItem {
        final String nome;
        final String cor;

        PromocaoItem(String nome, String cor) {
            this.nome = nome;
            this.cor = cor;
        }
    }
}
