package com.diegodev.apidesportes.jogos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.ActivityEsporte;
import com.diegodev.apidesportes.jogos.item.DataItem;

import java.util.List;

/**
 * Coluna lateral de navegação.
 * - Mostra a opção "Tabela" (classificação) ACIMA de "HOJE" quando um campeonato
 *   está selecionado.
 * - Mostra "HOJE" + as próximas datas.
 * - Mantém o destaque (selected) no item ativo: a data escolhida ou a Tabela.
 */
public class DataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_DATA = 1;
    private static final int VIEW_CLASSIFICACAO = 2;

    private final Context context;
    private final List<DataItem> items;
    private final ActivityEsporte fragment;

    private String dataSelecionada;
    private boolean classificacaoSelecionada;

    public DataAdapter(Context context, List<DataItem> items, ActivityEsporte fragment) {
        this.context = context;
        this.items = items;
        this.fragment = fragment;
        for (DataItem item : items) {
            if (item.tipo == DataItem.TIPO_DATA && item.primeiroDia) {
                this.dataSelecionada = item.valor; // "HOJE" vem selecionado por padrão
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).tipo == DataItem.TIPO_CLASSIFICACAO ? VIEW_CLASSIFICACAO : VIEW_DATA;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_CLASSIFICACAO) {
            return new ClassificacaoHolder(inflater.inflate(R.layout.api_item_classificacao_opcao, parent, false));
        }
        return new DataViewHolder(inflater.inflate(R.layout.api_item_data, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ClassificacaoHolder) {
            bindClassificacao((ClassificacaoHolder) holder);
        } else {
            bindData((DataViewHolder) holder, position);
        }
    }

    private void bindClassificacao(ClassificacaoHolder holder) {
        holder.itemView.setSelected(classificacaoSelecionada);
        holder.itemView.setOnClickListener(v -> {
            if (fragment != null) {
                classificacaoSelecionada = true;
                dataSelecionada = null;
                notifyDataSetChanged();
                fragment.buscarClassificacao();
            }
        });
    }

    private void bindData(DataViewHolder holder, int position) {
        DataItem item = items.get(position);
        String dataOriginal = item.valor;
        String nome = item.primeiroDia ? "HOJE" : dataOriginal;

        holder.tvData.setText(nome);
        holder.tvData.setSelected(dataSelecionada != null && dataSelecionada.equals(dataOriginal));

        // Na primeira data ("HOJE") apenas recebe o foco; o carregamento dos jogos
        // do dia é disparado UMA única vez pelo ActivityEsporte (autoCarregarHoje),
        // evitando buscas duplicadas/competindo quando um campeonato é selecionado.
        if (item.primeiroDia) {
            holder.tvData.requestFocus();
        }

        holder.tvData.setOnClickListener(v -> {
            if (fragment != null) {
                classificacaoSelecionada = false;
                dataSelecionada = dataOriginal;
                notifyDataSetChanged();
                fragment.buscarJogosPorData(dataOriginal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ClassificacaoHolder extends RecyclerView.ViewHolder {
        ClassificacaoHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class DataViewHolder extends RecyclerView.ViewHolder {
        TextView tvData;

        DataViewHolder(@NonNull View itemView) {
            super(itemView);
            tvData = itemView.findViewById(R.id.tvData);
        }
    }
}
