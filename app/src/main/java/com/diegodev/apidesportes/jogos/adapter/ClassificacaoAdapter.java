package com.diegodev.apidesportes.jogos.adapter;

import android.content.Context;
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

import java.util.List;

/**
 * Tabela de classificação. A posição 0 do adapter é o cabeçalho fixo com os
 * nomes das colunas (#, Time, J, V, E, D, Pts); as demais são as linhas.
 */
public class ClassificacaoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_HEADER = 0;
    private static final int VIEW_ROW = 1;

    private final Context context;
    private final List<ItemClassificacao> list;

    public ClassificacaoAdapter(Context context, List<ItemClassificacao> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_HEADER : VIEW_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.api_classificacao_header, parent, false));
        }
        return new RowHolder(inflater.inflate(R.layout.api_item_classificacao, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RowHolder) {
            RowHolder row = (RowHolder) holder;
            ItemClassificacao item = list.get(position - 1);

            row.tvPosicao.setText(String.valueOf(item.getPosicao()));
            row.tvJogos.setText(String.valueOf(item.getJogos()));
            row.tvVitorias.setText(String.valueOf(item.getVitorias()));
            row.tvEmpates.setText(String.valueOf(item.getEmpates()));
            row.tvDerrotas.setText(String.valueOf(item.getDerrotas()));
            row.tvPontos.setText(String.valueOf(item.getPontos()));
            row.tvTimeName.setText(item.getTimeName());

            // A API de classificação não envia logo; usa escudo genérico como placeholder.
            ImageLoader.load(context, item.getLogo(), row.ivTimeLogo, R.drawable.ic_time_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1; // +1 do cabeçalho
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        TextView tvPosicao, tvTimeName, tvJogos, tvVitorias, tvEmpates, tvDerrotas, tvPontos;
        ImageView ivTimeLogo;

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
        }
    }
}
