package com.diegodev.apidesportes.jogos.response;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.diegodev.apidesportes.jogos.bancoSql.ClassificacaoDatabase;
import com.diegodev.apidesportes.jogos.dialog.ExpiredDialogFragment;
import com.diegodev.apidesportes.jogos.interfac.ServiceClassificacao;
import com.diegodev.apidesportes.jogos.item.ItemClassificacao;
import com.diegodev.apidesportes.jogos.utils.ApiConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Busca a tabela de classificação de um campeonato e salva no cache local (Room).
 * Mesmo padrão do ApiMoviesCaller (jogos): Retrofit + Gson + JogosDatabase.
 *
 * Rota consumida: GET {baseUrl}campeonato/{id}/classificacao
 */
public class ApiClassificacaoCaller {

    private static final String TAG = "ApiClassificacao";
    private final Context context;

    public ApiClassificacaoCaller(Context context) {
        this.context = context;
    }

    public void chamarApi(String url, String token, int campId) {
        String authHeader = "Bearer " + token;
        String fullUrl = url + "campeonato/" + campId + "/classificacao";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServiceClassificacao apiService = retrofit.create(ServiceClassificacao.class);

        apiService.getClassificacao(fullUrl, authHeader).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String urlChamada = call.request().url().toString();
                    if (!ApiConfig.validateRequestedUrl(urlChamada)) {
                        System.exit(0);
                        return;
                    }

                    List<ItemClassificacao> itens = parseClassificacao(response.body(), campId);
                    if (itens == null || itens.isEmpty()) {
                        Log.w(TAG, "Nenhuma classificação retornada para o camp " + campId);
                        return;
                    }

                    new Thread(() -> {
                        ClassificacaoDatabase db = ClassificacaoDatabase.getInstance(context);
                        db.classificacaoDao().limparPorCamp(campId);
                        db.classificacaoDao().insertAll(itens);
                        Log.d(TAG, "Classificação salva no banco: " + itens.size() + " linhas (camp " + campId + ")");
                    }).start();
                } else {
                    int statusCode = response.code();
                    if (statusCode == 401) {
                        Log.e(TAG, "erro code " + statusCode);
                        try {
                            String errorBody = response.errorBody().string();
                            Gson gson = new Gson();
                            RpCategory.ErrorResponse errorResponse = gson.fromJson(errorBody, RpCategory.ErrorResponse.class);
                            if (errorResponse != null && !errorResponse.isRetorno()) {
                                Activity activity = (Activity) context;
                                activity.runOnUiThread(() -> {
                                    if (errorResponse.getError() != null && !errorResponse.getError().isEmpty()) {
                                        ExpiredDialogFragment.type_Expired = errorResponse.getError();
                                    }
                                    ExpiredDialogFragment dialog = new ExpiredDialogFragment();
                                    if (activity instanceof FragmentActivity) {
                                        ((FragmentActivity) activity).getSupportFragmentManager()
                                                .beginTransaction().add(dialog, "ExpiredDialog").commitAllowingStateLoss();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao ler corpo de erro", e);
                        }
                    } else {
                        Log.e(TAG, "Erro na resposta: " + statusCode);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.e(TAG, "Falha na requisição: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Converte o JSON da API em uma lista de ItemClassificacao.
     * Aceita array direto: [ {...}, {...} ] ou objeto com array dentro:
     * { "classificacao": [...] }, { "tabela": [...] }, { "data": [...] }, etc.
     */
    private List<ItemClassificacao> parseClassificacao(JsonElement body, int campId) {
        JsonArray array = null;
        if (body.isJsonArray()) {
            array = body.getAsJsonArray();
        } else if (body.isJsonObject()) {
            JsonObject obj = body.getAsJsonObject();
            for (String key : new String[]{"classificacao", "tabela", "times", "data", "resultado", "classification"}) {
                if (obj.has(key) && obj.get(key).isJsonArray()) {
                    array = obj.get(key).getAsJsonArray();
                    break;
                }
            }
        }
        if (array == null) {
            Log.w(TAG, "Formato de resposta não reconhecido (sem array de classificação).");
            return null;
        }

        Gson gson = new Gson();
        List<ItemClassificacao> itens = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }

            // Log do primeiro item para conferir os nomes reais dos campos da API
            if (itens.isEmpty()) {
                Log.d(TAG, "Exemplo do JSON recebido: " + element.toString());
            }

            ItemClassificacao item;
            try {
                item = gson.fromJson(element, ItemClassificacao.class);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao ler linha da classificação", e);
                item = null;
            }

            if (item != null) {
                JsonObject obj = element.getAsJsonObject();

                // 1) Nome/logo vindos do objeto "time"
                ItemClassificacao.Time time = item.getTime();
                if (time != null) {
                    item.setTimeName(time.getNome());
                    item.setLogo(time.getLogo());
                }

                // 2) Fallback: procura nome/logo em outros nomes de campo comuns
                if (isEmpty(item.getTimeName())) {
                    item.setTimeName(lerTexto(obj, "time_name", "team_name", "nome_time",
                            "time_nome", "nome", "name", "team", "nome_popular"));
                }
                if (isEmpty(item.getLogo())) {
                    item.setLogo(lerTexto(obj, "logo", "escudo", "time_logo",
                            "team_logo", "logo_url", "url_logo"));
                }

                item.setCampId(campId);
                itens.add(item);
            }
        }
        return itens;
    }

    /** Lê o primeiro campo de texto existente no JsonObject (array de chaves candidatas). */
    private String lerTexto(JsonObject obj, String... chaves) {
        for (String chave : chaves) {
            if (obj.has(chave)) {
                JsonElement valor = obj.get(chave);
                if (valor.isJsonPrimitive()) {
                    String texto = valor.getAsString();
                    if (!isEmpty(texto)) {
                        return texto;
                    }
                } else if (valor.isJsonObject()) {
                    JsonObject sub = valor.getAsJsonObject();
                    for (String subChave : new String[]{"nome", "name", "nome_popular", "logo", "escudo"}) {
                        if (sub.has(subChave) && sub.get(subChave).isJsonPrimitive()) {
                            String texto = sub.get(subChave).getAsString();
                            if (!isEmpty(texto)) {
                                return texto;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isEmpty(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
