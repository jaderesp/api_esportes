package com.diegodev.apidesportes.jogos.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.utils.ImageLoader;
import com.diegodev.apidesportes.jogos.utils.JogoStatus;

import java.util.List;

public class JogosAdapter extends RecyclerView.Adapter<JogosAdapter.ViewHolder> {

    private static final String TAG = "AdapterJogos";
    private List<ItemJogos> list;
    private Context context;
    private OnItemClickListener onItemClickListener;
    private OnCanalClickListener onCanalClickListener;

    /** Callback disparado quando o usuário clica em um jogo. */
    public interface OnItemClickListener {
        void onItemClick(ItemJogos jogo);
    }

    /** Callback disparado quando o usuário clica em um canal (canais_links) da linha do jogo. */
    public interface OnCanalClickListener {
        void onCanalClick(ItemJogos jogo, ItemCanalLink canal);
    }

    public JogosAdapter(Context context, List<ItemJogos> list) {
        this.context = context;
        this.list = list;
    }

    /** Registra o clique no jogo (ex.: para abrir o modal de canais). */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /** Registra o clique em um canal da linha (ex.: para abrir o modal do canal). */
    public void setOnCanalClickListener(OnCanalClickListener listener) {
        this.onCanalClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.api_item_jogos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemJogos itemJogos = list.get(position);

        String descricao = itemJogos.getDescription();
        int gola = itemJogos.getGolsA();
        int golb = itemJogos.getGolsB();

        // Status do jogo (traduz o campo "description" da API) + pill do widget
        holder.txtdescricao.setText(JogoStatus.texto(descricao));
        holder.txtdescricao.setBackgroundResource(JogoStatus.pillRes(descricao));
        if (JogoStatus.temPlacar(descricao)) {
            holder.txtPlacar.setVisibility(View.VISIBLE);
            holder.imgvs.setVisibility(View.INVISIBLE);
            holder.txtPlacar.setText(gola + "-" + golb);
        } else {
            holder.txtPlacar.setVisibility(View.INVISIBLE);
            holder.imgvs.setVisibility(View.VISIBLE);
        }

        // Carregar logos (vazio / URL / base64)
        ImageLoader.load(context, itemJogos.getLogoA(), holder.TeamA);
        ImageLoader.load(context, itemJogos.getLogoB(), holder.TeamB);
        ImageLoader.load(context, itemJogos.getLogoCamp(), holder.logocamp);

        // Exibir textos
        holder.TimeA.setText(itemJogos.getTimeA());
        holder.TimeB.setText(itemJogos.getTimeB());
        holder.txtTime.setText(itemJogos.getStart());
        holder.campname.setText(itemJogos.getCampName());

        // Clique no jogo -> callback (abre o modal de canais na Activity)
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(itemJogos);
            }
        });

        // Faixa de canais de transmissão (canais_links) abaixo da linha do jogo
        preencherCanais(holder, itemJogos);

        // Logs para depuração
        Log.d(TAG, "Logo Time A: " + itemJogos.getLogoA());
        Log.d(TAG, "Logo Time B: " + itemJogos.getLogoB());
    }

    /**
     * Exibe APENAS os canais_links do jogo como chips focáveis abaixo da linha.
     * Cada chip, ao ser clicado, dispara o callback para abrir o modal do canal.
     */
    private void preencherCanais(ViewHolder holder, ItemJogos jogo) {
        LinearLayout container = holder.containerCanaisLinha;
        HorizontalScrollView scroll = holder.scrollCanaisLinha;
        if (container == null || scroll == null) {
            return;
        }

        container.removeAllViews();

        List<ItemCanalLink> links = jogo.getCanaisLinks();
        boolean temLinks = links != null && !links.isEmpty();

        if (!temLinks) {
            scroll.setVisibility(View.GONE);
            return;
        }

        scroll.setVisibility(View.VISIBLE);

        for (ItemCanalLink canal : links) {
            if (canal == null) {
                continue;
            }
            View chip = inflarChip(container, canal.getChannelName(), canal.getChannelLogo());
            chip.setOnClickListener(v -> {
                if (onCanalClickListener != null) {
                    onCanalClickListener.onCanalClick(jogo, canal);
                }
            });
            container.addView(chip);
        }
    }

    /** Cria um chip de canal com nome e logotipo (sem anexar ao container). */
    private View inflarChip(LinearLayout container, String nome, String logo) {
        View chip = LayoutInflater.from(context)
                .inflate(R.layout.api_item_canal_linha, container, false);

        TextView txtNome = chip.findViewById(R.id.tv_canal_nome);
        txtNome.setText(nome == null || nome.trim().isEmpty() ? "Canal" : nome.trim());

        ImageView logoView = chip.findViewById(R.id.iv_canal_logo);
        ImageLoader.load(context, logo, logoView);

        return chip;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView TeamA, TeamB, imgvs, logocamp;
        TextView txtTime, TimeA, TimeB, txtdescricao, txtPlacar, campname;
        HorizontalScrollView scrollCanaisLinha;
        LinearLayout containerCanaisLinha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            TeamA = itemView.findViewById(R.id.iv_iconA);
            TeamB = itemView.findViewById(R.id.iv_iconB);
            txtTime = itemView.findViewById(R.id.tv_time);
            TimeA = itemView.findViewById(R.id.tv_name1);
            TimeB = itemView.findViewById(R.id.tv_name2);
            imgvs = itemView.findViewById(R.id.imgvs);
            txtdescricao = itemView.findViewById(R.id.txtdescricao);
            txtPlacar = itemView.findViewById(R.id.txtPlacar);
            logocamp = itemView.findViewById(R.id.iv_iconCamp);
            campname = itemView.findViewById(R.id.tv_nameCamp);
            scrollCanaisLinha = itemView.findViewById(R.id.scrollCanaisLinha);
            containerCanaisLinha = itemView.findViewById(R.id.containerCanaisLinha);
        }
    }
}
