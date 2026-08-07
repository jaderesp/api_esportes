package com.diegodev.apidesportes.jogos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.diegodev.apidesportes.R;

/**
 * Carrega uma imagem em um ImageView de forma segura.
 * A API pode devolver a logo de 3 formas:
 *  - vazia/null  -> usa o drawable padrão
 *  - URL (http)  -> carrega com Glide
 *  - Base64      -> decodifica para Bitmap e carrega com Glide
 * Centralizado aqui para reutilizar na lista de jogos e no modal de canais.
 */
public final class ImageLoader {

    private static final String TAG = "ImageLoader";

    private ImageLoader() {
    }

    /** Carrega usando o placeholder padrão do app. */
    public static void load(Context context, String image, ImageView imageView) {
        load(context, image, imageView, R.drawable.ic_launcher_foreground);
    }

    /** Carrega usando um placeholder específico (ex.: escudo genérico do time). */
    public static void load(Context context, String image, ImageView imageView, int placeholderRes) {
        if (imageView == null) {
            return;
        }

        // 1) Vazio ou nulo -> imagem padrão
        if (image == null || image.trim().isEmpty()) {
            imageView.setImageResource(placeholderRes);
            return;
        }

        // 2) URL -> Glide carrega direto
        if (image.startsWith("http")) {
            Glide.with(context)
                    .load(image)
                    .placeholder(placeholderRes)
                    .error(android.R.drawable.stat_notify_error)
                    .into(imageView);
            return;
        }

        // 3) Base64 -> converte para Bitmap antes
        Bitmap bitmap = base64ToBitmap(image);
        if (bitmap != null) {
            Glide.with(context)
                    .load(bitmap)
                    .placeholder(placeholderRes)
                    .error(android.R.drawable.stat_notify_error)
                    .into(imageView);
        } else {
            imageView.setImageResource(placeholderRes);
        }
    }

    private static Bitmap base64ToBitmap(String base64String) {
        try {
            if (!base64String.startsWith("data:image")) {
                Log.e(TAG, "A string não começa com 'data:image'. Valor: " + base64String);
                return null;
            }
            // Remove o prefixo "data:image/png;base64,"
            String base64Data = base64String.substring(base64String.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter Base64 em Bitmap: " + e.getMessage(), e);
            return null;
        }
    }
}
