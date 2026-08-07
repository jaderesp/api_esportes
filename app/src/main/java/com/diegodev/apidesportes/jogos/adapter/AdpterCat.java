package com.diegodev.apidesportes.jogos.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.ActivityEsporte;
import com.diegodev.apidesportes.jogos.item.ItemCat;


import java.util.List;

public class AdpterCat extends RecyclerView.Adapter<AdpterCat.ViewHolder> {


    private List<ItemCat> list;
    private boolean inicio = false;
    private Context context;
    private ActivityEsporte fragment;
    private int campanhaSelecionadaId = -1;

    public AdpterCat(Context context, List<ItemCat> list, ActivityEsporte fragment) {
        this.context = context;
        this.list = list;
        this.fragment = fragment;

    }

    /** Marca visualmente o campeonato selecionado (a opção "Tabela" fica disponível). */
    public void setCampanhaSelecionada(int id) {
        this.campanhaSelecionadaId = id;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.api_item_camp, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ItemCat itemCategory = list.get(position);


        Log.d("AdpterCate", "Posição: " + position + " - Categoria: " + itemCategory.getCategoryname());

        String categoryName = itemCategory.getCategoryname();
        String logo = itemCategory.getLogo();
        int categoryId = itemCategory.getCategory();

        // Destaca o campeonato selecionado (fundo azul via sport_selector)
        holder.categorychannel.setSelected(categoryId == campanhaSelecionadaId);


        if (categoryName != null) {

          holder.TeamA.setText(categoryName);

            Log.d("AdpterCate", "Nome da categoria  "+categoryName);

        }

        if (logo != null && !logo.isEmpty()) {

            Glide.with(context)
                    .load(logo)
                    .into(holder.CampLogo);
        } else {
            Log.e("Adapter", "Contexto ou URL da imagem é nulo.");
        }


        holder.categorychannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fragment != null) {
                    fragment.buscarJogosPorId(categoryId);
                }
            }
        });


/*

        int categoryId = itemCategory.getCategory();

        if (position == 0 && !inicio) {

            inicio = true;

            holder.categorychannel.requestFocus();
            String name = itemCategory.getCategoryname();
            ((Ondeman) activity).loading();
            ((Ondeman) activity).ApiMOvies(categoryId,name);

        }



        holder.categorychannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean liberarAdulto = PlayerActivity.LiberarAdulto;

                if (categoryName.toLowerCase().contains("adulto") || categoryName.toLowerCase().contains("18")) {
                    if (liberarAdulto){
                        String name = itemCategory.getCategoryname();
                        ((Ondeman) activity).loading();
                        ((Ondeman) activity).ApiMOvies(categoryId,name);
                    }else{
                        DialogAdulto dialogAdulto = new DialogAdulto(context);
                        dialogAdulto.showDialog(context);
                    }

                } else {

                    String name = itemCategory.getCategoryname();
                    ((Ondeman) activity).loading();
                    ((Ondeman) activity).ApiMOvies(categoryId,name);
                }

            }
        });


 */


        }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView TeamA;
        LinearLayout categorychannel;

        ImageView CampLogo;


        public ViewHolder(View itemView) {
            super(itemView);
            TeamA = itemView.findViewById(R.id.CampName);
            categorychannel = itemView.findViewById(R.id.linearcamp);
            CampLogo = itemView.findViewById(R.id.CampLogo);

        }
    }
}
