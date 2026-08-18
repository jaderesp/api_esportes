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
import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.utils.ImageLoader;
import com.diegodev.apidesportes.jogos.utils.JogoStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
        // Passa os objetos completos (canais_links) para exibir logotipos no modal.
        // O modal exibe APENAS os canais da playlist (canais_links): só eles têm a
        // transmission_url para reprodução. Os demais índices (canais, canais_ia,
        // canais_simples) não são listados — canais sem URL não são clicáveis.
        if (jogo.getCanaisLinks() != null && !jogo.getCanaisLinks().isEmpty()) {
            args.putString(ARG_CANAIS_LINKS_JSON, new Gson().toJson(jogo.getCanaisLinks()));
        }
        fragment.setArguments(args);
        return fragment;
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

    /**
     * Monta os canais dentro do ScrollView. Exibe APENAS os canais_links
     * (playlist): só eles possuem a {@code transmission_url} para reprodução.
     * Os demais índices (canais, canais_ia, canais_simples) NÃO são listados —
     * canais sem URL de transmissão não são clicáveis.
     */
    private void preencherCanais() {
        if (adicionarSecaoLinks()) {
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

        // Um chip (cartão) para cada canal link, com logotipo (se existir) e nome
        for (ItemCanalLink canal : links) {
            if (canal == null) {
                continue;
            }
            // Chip é um container horizontal: logo (se houver) + nome.
            LinearLayout chip = new LinearLayout(requireContext());
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(Gravity.CENTER_VERTICAL);
            chip.setPadding(dp(10), dp(8), dp(10), dp(8));
            chip.setBackgroundResource(R.drawable.bg_canal_chip_selector_modal);
            chip.setFocusable(true); // foco com controle remoto (TV)

            String nome = canal.getChannelName();
            String nomeExibicao = nome == null || nome.trim().isEmpty() ? "Canal" : nome.trim();

            // Logo do canal: só é exibida quando a API informa channel_logo.
            String logo = canal.getChannelLogo();
            if (logo != null && !logo.trim().isEmpty()) {
                ImageView ivLogo = new ImageView(requireContext());
                int logoSize = dp(20);
                LinearLayout.LayoutParams logoParams =
                        new LinearLayout.LayoutParams(logoSize, logoSize);
                logoParams.setMarginEnd(dp(8));
                ivLogo.setLayoutParams(logoParams);
                ivLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageLoader.load(requireContext(), logo.trim(), ivLogo);
                chip.addView(ivLogo);
            }

            TextView tvNome = new TextView(requireContext());
            tvNome.setText(nomeExibicao);
            tvNome.setTextColor(getResources().getColor(R.color.white));
            tvNome.setTextSize(14);
            tvNome.setGravity(Gravity.CENTER);
            chip.addView(tvNome);

            // Clique no canal: notifica o app consumidor com o stream_id e, se o
            // app estiver ouvindo o evento, encerra a tela do SDK (requireActivity
            // .finish()) para o app assumir a transmissão.
            // Sem stream_id válido o evento NÃO é disparado (nunca envia 0/-1).
            chip.setOnClickListener(v -> {
                if (canal.getStreamId() != null) {
                    EsporteEventListener.notificarCanalClicado(canal.getStreamId());
                    if (EsporteEventListener.possuiListenerDeCanal()) {
                        requireActivity().finish();
                    }
                }
            });
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
