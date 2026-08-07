package com.diegodev.apidesportes.jogos.interfac;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Url;

/**
 * Interface Retrofit da rota de classificação.
 * Usamos JsonElement no retorno porque a estrutura da resposta pode variar
 * (array direto ou objeto com array dentro); o parse é feito no
 * ApiClassificacaoCaller.
 */
public interface ServiceClassificacao {
    @GET
    Call<JsonElement> getClassificacao(@Url String url, @Header("Authorization") String authHeader);
}
