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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.utils.ImageLoader;

/**
 * Modal (bottom sheet) com os detalhes de um canal de transmissão:
 * logotipo, nome, servidor e URL de transmissão.
 *
 * COMO USAR (em qualquer Activity):
 *   CanalDetalheDialogFragment.newInstance(canal).show(getSupportFragmentManager(), "canal_detalhe");
 */
public class CanalDetalheDialogFragment extends DialogFragment {

    private static final String ARG_EXTERNAL_CHANNEL_ID = "external_channel_id";
    private static final String ARG_CHANNEL_NAME = "channel_name";
    private static final String ARG_CHANNEL_LOGO = "channel_logo";
    private static final String ARG_SERVER_NAME = "server_name";
    private static final String ARG_SERVER_SLUG = "server_slug";
    private static final String ARG_TRANSMISSION_URL = "transmission_url";

    public static CanalDetalheDialogFragment newInstance(ItemCanalLink canal) {
        CanalDetalheDialogFragment fragment = new CanalDetalheDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EXTERNAL_CHANNEL_ID, canal.getExternalChannelId());
        args.putString(ARG_CHANNEL_NAME, canal.getChannelName());
        args.putString(ARG_CHANNEL_LOGO, canal.getChannelLogo());
        args.putString(ARG_SERVER_NAME, canal.getServerName());
        args.putString(ARG_SERVER_SLUG, canal.getServerSlug());
        args.putString(ARG_TRANSMISSION_URL, canal.getTransmissionUrl());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_canal_detalhe, container, false);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.TransparentDialog);

        ImageButton btnClose = view.findViewById(R.id.btnCloseCanal);
        btnClose.setOnClickListener(v -> dismiss());

        preencherDetalhes(view);

        return view;
    }

    private void preencherDetalhes(View view) {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        TextView txtNome = view.findViewById(R.id.txtCanalNome);
        String nome = args.getString(ARG_CHANNEL_NAME);
        txtNome.setText(nome == null || nome.trim().isEmpty() ? "Canal" : nome.trim());

        TextView txtServidor = view.findViewById(R.id.txtCanalServidor);
        String serverName = args.getString(ARG_SERVER_NAME);
        String serverSlug = args.getString(ARG_SERVER_SLUG);
        txtServidor.setText("Servidor: " + (serverName != null && !serverName.trim().isEmpty()
                ? serverName.trim()
                : (serverSlug != null && !serverSlug.trim().isEmpty() ? serverSlug.trim() : "—")));

        TextView txtUrl = view.findViewById(R.id.txtTransmissionUrl);
        String url = args.getString(ARG_TRANSMISSION_URL);
        txtUrl.setText(url == null || url.trim().isEmpty() ? "—" : url.trim());

        ImageLoader.load(requireContext(), args.getString(ARG_CHANNEL_LOGO), view.findViewById(R.id.ivCanalLogo));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }
}
