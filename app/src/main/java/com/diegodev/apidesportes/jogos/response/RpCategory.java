package com.diegodev.apidesportes.jogos.response;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.fragment.app.FragmentActivity;
import com.diegodev.apidesportes.jogos.dialog.ExpiredDialogFragment;
import com.diegodev.apidesportes.jogos.item.ItemCat;
import com.diegodev.apidesportes.jogos.interfac.ServiceCate;
import com.diegodev.apidesportes.jogos.utils.UnsafeOkHttpClient;
import com.diegodev.apidesportes.jogos.utils.ApiConfig;
import com.diegodev.apidesportes.jogos.bancoSql.CategoriaDatabase;
import com.google.gson.Gson;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RpCategory {


    private Context context;
    private ServiceCate apiService;

    // Construtor
    public RpCategory(Context activity) {

        this.context = activity;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.getBaseUrl())
                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ServiceCate.class);
    }

    // Método executando a API diretamente, sem listener
    public void execute(String fullUrl, String token) {
        String authHeader = "Bearer " + token;

        Call<List<ItemCat>> call = apiService.getOndemanCategories(fullUrl, authHeader);
        call.enqueue(new Callback<List<ItemCat>>() {
            @Override
            public void onResponse(Call<List<ItemCat>> call, Response<List<ItemCat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String urlChamada = call.request().url().toString();
                    if (!ApiConfig.validateRequestedUrl(urlChamada)) {
                        System.exit(0);
                        return;
                    }
                    List<ItemCat> itemCategories = response.body();
                    Log.d("categoryjogos", "Resposta do servidor: " + itemCategories);

                    if (!itemCategories.isEmpty()) {
                        // Salva no banco Room em background
                        new Thread(() -> {
                            CategoriaDatabase db = CategoriaDatabase.getInstance(context); // certifique-se que 'context' está acessível aqui
                            db.categoriaDao().limpar(); // se quiser limpar antes
                            db.categoriaDao().insertAll(itemCategories);
                            Log.d("categoryjogos", "Itens salvos no banco de dados Room: " + itemCategories.size());
                        }).start();
                    }
                } else {
                    int statusCode = response.code();
                    if (statusCode == 401) {

                        Log.e("categoryjogos", "erro code" + statusCode);
                        // Tentar obter o corpo de erro como string
                        try {
                            String errorBody = response.errorBody().string();
                            Gson gson = new Gson();
                            ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);

                            if(!errorResponse.isRetorno()){

                                Log.e("categoryjogos", "dentro do if " + statusCode);

                                Activity activity = (Activity) context;
                                activity.runOnUiThread(() -> {
                                    if(!errorResponse.getError().isEmpty()){
                                        ExpiredDialogFragment.type_Expired = errorResponse.getError();
                                    }

                                    ExpiredDialogFragment dialog = new ExpiredDialogFragment();

                                    Log.e("categoryjogos", "dentro da ruintrend");

                                    if (activity instanceof FragmentActivity) {
                                        FragmentActivity fragmentActivity = (FragmentActivity) activity;
                                        dialog.show(fragmentActivity.getSupportFragmentManager(), "ExpiredDialog");

                                    } else {
                                        Log.e("categoryjogos", "A Activity não é uma FragmentActivity. O diálogo não pode ser exibido.");
                                    }
                                });

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("categoryjogos", "Erro na resposta: " + statusCode);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ItemCat>> call, Throwable t) {
                Log.e("categoryjogos", "Falha na requisição: " + t.getMessage(), t);
            }
        });
    }

    public class ErrorResponse {
        private String error;
        private boolean retorno;

        public String getError() {
            return error;
        }

        public boolean isRetorno() {
            return retorno;
        }
    }


}
