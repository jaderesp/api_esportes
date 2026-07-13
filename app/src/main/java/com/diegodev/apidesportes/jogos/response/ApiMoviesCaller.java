package com.diegodev.apidesportes.jogos.response;

import android.content.Context;
import android.util.Log;

import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.interfac.ServiceJogos;
import com.diegodev.apidesportes.jogos.bancoSql.JogosDatabase;
import com.diegodev.apidesportes.jogos.utils.ApiConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiMoviesCaller {

    private static final String TAG = "ApiMoviesCaller";
    private final Context context;

    private String categoriesMovies;

    public ApiMoviesCaller(Context context) {
        this.context = context;

    }

    public void chamarApiMovies(String url, String token) {
        Log.d(TAG, "chamarApiMovies: Iniciando");

        String authHeader = "Bearer " + token;

        categoriesMovies = url + "jogos";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServiceJogos apiService = retrofit.create(ServiceJogos.class);

        apiService.getJogos(categoriesMovies, authHeader).enqueue(new Callback<List<ItemJogos>>() {
            @Override
            public void onResponse(Call<List<ItemJogos>> call, Response<List<ItemJogos>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItemJogos> itemCategories = response.body();
                    Log.d(TAG, "Resposta do servidor: " + itemCategories);

                    String urlChamada = call.request().url().toString();
                    if (!ApiConfig.validateRequestedUrl(urlChamada)) {
                        System.exit(0);
                        return;
                    }

                    if (!itemCategories.isEmpty()) {
                        new Thread(() -> {
                            // Instância do banco
                            JogosDatabase db = JogosDatabase.getInstance(context);

                            // Conversão de data
                            List<ItemJogos> convertidos = new ArrayList<>();
                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                            isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            SimpleDateFormat formatBR = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
                            formatBR.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

                            for (ItemJogos item : itemCategories) {
                                try {
                                    Date dataUtc = isoFormat.parse(item.getStart());
                                    String dataConvertida = formatBR.format(dataUtc);
                                    item.setStart(dataConvertida);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    item.setStart(""); // fallback vazio se falhar
                                }


                                if (item.getCampeonato() != null) {
                                    item.setCampName(item.getCampeonato().getCampName());
                                    item.setLogoCamp(item.getCampeonato().getLogoCamp());
                                    item.setCampId(item.getCampeonato().getCampId());
                                }

                                convertidos.add(item);
                            }
                            // Salvar no banco
                            db.jogosDao().limpar();
                            db.jogosDao().insertAll(convertidos);
                        }).start();
                    }
                } else {
                    Log.e(TAG, "Erro na resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ItemJogos>> call, Throwable t) {
                Log.e(TAG, "Falha na requisição: " + t.getMessage(), t);
            }
        });

    }
}
