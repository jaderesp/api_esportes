package com.diegodev.apidesportes.jogos.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.event.EsporteEventListener;
import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.item.ItemCanalSimples;
import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.utils.ImageLoader;
import com.diegodev.apidesportes.jogos.utils.JogoStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal (bottom sheet) que exibe os canais de transmissão de um jogo.
 *
 * COMO USAR (em qualquer Activity):
 *   CanaisDialogFragment.newInstance(jogo).show(getSupportFragmentManager(), "canais_dialog");
 *
 * O botão voltar e o toque fora do painel fecham o modal (UX padrão Android).
 */
public class CanaisDialogFragment extends DialogFragment {

    // Chaves do Bundle com os dados do jogo (recebidos em newInstance).
    private static final String ARG_CAMP_NAME = "camp_name";
    private static final String ARG_CAMP_LOGO = "camp_logo";
    private static final String ARG_START = "start";
    private static final String ARG_DESC = "descricao";
    private static final String ARG_TEAM_A = "team_a";
    private static final String ARG_TEAM_B = "team_b";
    private static final String ARG_LOGO_A = "logo_a";
    private static final String ARG_LOGO_B = "logo_b";
    private static final String ARG_GOLS_A = "gols_a";
    private static final String ARG_GOLS_B = "gols_b";
    private static final String ARG_CANAIS = "canais";
    private static final String ARG_CANAIS_IA = "canais_ia";
    private static final String ARG_CANAIS_SIMPLES = "canais_simples";
    private static final String ARG_CANAIS_LINKS = "canais_links";
    private static final String ARG_CANAIS_LINKS_JSON = "canais_links_json";

    private LinearLayout containerCanais;

    public static CanaisDialogFragment newInstance(ItemJogos jogo) {
        CanaisDialogFragment fragment = new CanaisDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMP_NAME, jogo.getCampName());
        args.putString(ARG_CAMP_LOGO, jogo.getLogoCamp());
        args.putString(ARG_START, jogo.getStart());
        args.putString(ARG_DESC, jogo.getDescription());
        args.putString(ARG_TEAM_A, jogo.getTimeA());
        args.putString(ARG_TEAM_B, jogo.getTimeB());
        args.putString(ARG_LOGO_A, jogo.getLogoA());
        args.putString(ARG_LOGO_B, jogo.getLogoB());
        args.putInt(ARG_GOLS_A, jogo.getGolsA());
        args.putInt(ARG_GOLS_B, jogo.getGolsB());
        args.putStringArrayList(ARG_CANAIS, toArrayList(jogo.getCanais()));
        args.putStringArrayList(ARG_CANAIS_IA, toArrayList(jogo.getCanaisIa()));
        args.putStringArrayList(ARG_CANAIS_SIMPLES, nomesSimples(jogo.getCanaisSimples()));
        args.putStringArrayList(ARG_CANAIS_LINKS, nomesLinks(jogo.getCanaisLinks()));
        // Passa os objetos completos (canais_links) para exibir logotipos no modal.
        if (jogo.getCanaisLinks() != null && !jogo.getCanaisLinks().isEmpty()) {
            args.putString(ARG_CANAIS_LINKS_JSON, new Gson().toJson(jogo.getCanaisLinks()));
        }
        fragment.setArguments(args);
        return fragment;
    }

    /** Null-safe: o Bundle não aceita lista vazia como nula. */
    private static ArrayList<String> toArrayList(List<String> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    /** Extrai apenas os nomes dos canais simples (objetos) para exibir no modal. */
    private static ArrayList<String> nomesSimples(List<ItemCanalSimples> canais) {
        ArrayList<String> nomes = new ArrayList<>();
        if (canais != null) {
            for (ItemCanalSimples canal : canais) {
                if (canal != null && canal.getChannelName() != null && !canal.getChannelName().trim().isEmpty()) {
                    nomes.add(canal.getChannelName().trim());
                }
            }
        }
        return nomes;
    }

    /** Extrai apenas os nomes dos canais link (objetos) para exibir no modal. */
    private static ArrayList<String> nomesLinks(List<ItemCanalLink> canais) {
        ArrayList<String> nomes = new ArrayList<>();
        if (canais != null) {
            for (ItemCanalLink canal : canais) {
                if (canal != null && canal.getChannelName() != null && !canal.getChannelName().trim().isEmpty()) {
                    nomes.add(canal.getChannelName().trim());
                }
            }
        }
        return nomes;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_canais_jogos, container, false);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialog);

        containerCanais = view.findViewById(R.id.containerCanais);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dismiss());

        preencherCabecalho(view);
        preencherCanais();

        return view;
    }

    /** Preenche título, resumo do jogo e placar no topo do modal. */
    private void preencherCabecalho(View view) {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        TextView txtCampName = view.findViewById(R.id.txtCampName);
        txtCampName.setText(args.getString(ARG_CAMP_NAME));

        TextView txtGameInfo = view.findViewById(R.id.txtGameInfo);
        String hora = args.getString(ARG_START, "");
        String status = JogoStatus.texto(args.getString(ARG_DESC));
        txtGameInfo.setText(hora + " • " + status);

        ImageLoader.load(requireContext(), args.getString(ARG_CAMP_LOGO), view.findViewById(R.id.ivCampLogo));

        TextView txtTeamA = view.findViewById(R.id.txtTeamA);
        txtTeamA.setText(args.getString(ARG_TEAM_A));
        TextView txtTeamB = view.findViewById(R.id.txtTeamB);
        txtTeamB.setText(args.getString(ARG_TEAM_B));

        ImageLoader.load(requireContext(), args.getString(ARG_LOGO_A), view.findViewById(R.id.ivTeamA));
        ImageLoader.load(requireContext(), args.getString(ARG_LOGO_B), view.findViewById(R.id.ivTeamB));

        TextView txtPlacar = view.findViewById(R.id.txtPlacar);
        if (JogoStatus.temPlacar(args.getString(ARG_DESC))) {
            txtPlacar.setText(args.getInt(ARG_GOLS_A) + "-" + args.getInt(ARG_GOLS_B));
        } else {
            txtPlacar.setText("vs");
        }
    }

    /** Monta as seções de canais dentro do ScrollView. Os canais_links vêm primeiro. */
    private void preencherCanais() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        // Primeiro os canais_links (com logotipo e dados de transmissão),
        // depois as demais listas simples de nomes.
        boolean vazio = adicionarSecaoLinks()
                && adicionarSecao(R.string.modal_canais_sec_simples, args.getStringArrayList(ARG_CANAIS_SIMPLES))
                && adicionarSecao(R.string.modal_canais_sec_tv, args.getStringArrayList(ARG_CANAIS))
                && adicionarSecao(R.string.modal_canais_sec_ia, args.getStringArrayList(ARG_CANAIS_IA));

        if (vazio) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.modal_canais_empty);
            empty.setTextColor(getResources().getColor(R.color.modal_text_secondary));
            empty.setGravity(Gravity.CENTER);
            empty.setTextSize(12);
            containerCanais.addView(empty);
        }
    }

    /**
     * Adiciona a seção com os canais_links (objeto completo) exibindo
     * logotipo + nome em chip bonito.
     * @return true se não havia canais links (para saber se está tudo vazio).
     */
    private boolean adicionarSecaoLinks() {
        Bundle args = getArguments();
        if (args == null || !args.containsKey(ARG_CANAIS_LINKS_JSON)) {
            return true;
        }

        String json = args.getString(ARG_CANAIS_LINKS_JSON);
        Type type = new TypeToken<List<ItemCanalLink>>() {}.getType();
        List<ItemCanalLink> links;
        try {
            links = new Gson().fromJson(json, type);
        } catch (Exception e) {
            return true;
        }
        if (links == null || links.isEmpty()) {
            return true;
        }

        // Título da seção
        TextView titulo = new TextView(requireContext());
        titulo.setText(R.string.modal_canais_sec_links);
        titulo.setTextColor(getResources().getColor(R.color.modal_section_title));
        titulo.setTextSize(12);
        containerCanais.addView(titulo);

        // Um chip (cartão) para cada canal link, com logotipo e nome
        for (ItemCanalLink canal : links) {
            if (canal == null) {
                continue;
            }
            TextView chip = new TextView(requireContext());
            String nome = canal.getChannelName();
            chip.setText(nome == null || nome.trim().isEmpty() ? "Canal" : nome.trim());
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setTextSize(14);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(10), dp(8), dp(10), dp(8));
            chip.setBackgroundResource(R.drawable.bg_canal_chip);
            chip.setFocusable(true); // foco com controle remoto (TV)
            // Clique no canal: notifica o app consumidor com o stream_id.
            // Se o app consumir (true), o SDK não executa ação padrão.
            chip.setOnClickListener(v -> EsporteEventListener.notificarCanalClicado(canal.getStreamId()));
            containerCanais.addView(chip);
        }

        // Espaço entre seções
        View espaco = new View(requireContext());
        espaco.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(4)));
        containerCanais.addView(espaco);

        return false;
    }

    /**
     * Adiciona uma seção com título + um chip por canal.
     * @return true se a lista estava vazia (para saber se tudo está vazio).
     */
    private boolean adicionarSecao(int tituloRes, List<String> canais) {
        if (canais == null || canais.isEmpty()) {
            return true;
        }

        // Título da seção
        TextView titulo = new TextView(requireContext());
        titulo.setText(tituloRes);
        titulo.setTextColor(getResources().getColor(R.color.modal_section_title));
        titulo.setTextSize(12);
        containerCanais.addView(titulo);

        // Um chip (cartão) para cada canal
        for (String canal : canais) {
            if (canal == null || canal.trim().isEmpty()) {
                continue;
            }
            TextView chip = new TextView(requireContext());
            chip.setText(canal.trim());
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setTextSize(14);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(10), dp(8), dp(10), dp(8));
            chip.setBackgroundResource(R.drawable.bg_canal_chip);
            chip.setFocusable(true); // foco com controle remoto (TV)
            containerCanais.addView(chip);
        }

        // Espaço entre seções
        View espaco = new View(requireContext());
        espaco.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(4)));
        containerCanais.addView(espaco);

        return false;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Boa UX: toque fora e botão voltar fecham o modal (padrão já ativo).
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Posiciona o painel na parte de baixo da tela, ocupando a largura toda.
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            // Sem dim padrão: usamos nosso próprio fundo escurecido (scrim) no layout.
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
