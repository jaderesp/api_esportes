package com.diegodev.apidesportes.jogos.bancoSql;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Converters do Room.
 * O banco não sabe salvar "List<String>". Este conversor transforma a lista
 * em um texto JSON na hora de salvar e volta para lista na hora de ler.
 * Usado pelos campos de canais (canais, canais_ia, canais_simples, canais_links)
 * do ItemJogos. Registrar em JogosDatabase com @TypeConverters({Converters.class}).
 */
public class Converters {

    private static final Gson GSON = new Gson();
    private static final Type TYPE_LIST_STRING = new TypeToken<List<String>>() {}.getType();

    /** Lista -> texto JSON (para gravar no banco). */
    @TypeConverter
    public static String fromList(List<String> value) {
        return value == null ? null : GSON.toJson(value);
    }

    /** Texto JSON -> Lista (para ler do banco). */
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return GSON.fromJson(value, TYPE_LIST_STRING);
        } catch (Exception e) {
            // Se o texto salvo estiver corrompido, devolve lista vazia em vez de quebrar.
            return null;
        }
    }
}
