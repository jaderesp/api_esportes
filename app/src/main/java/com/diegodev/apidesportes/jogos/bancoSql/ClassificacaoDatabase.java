package com.diegodev.apidesportes.jogos.bancoSql;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.diegodev.apidesportes.jogos.item.ItemClassificacao;

/** Cache local da tabela de classificação. Mesmo padrão do JogosDatabase. */
@Database(entities = {ItemClassificacao.class}, version = 1)
public abstract class ClassificacaoDatabase extends RoomDatabase {

    private static volatile ClassificacaoDatabase INSTANCE;

    public abstract ClassificacaoDao classificacaoDao();

    public static ClassificacaoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ClassificacaoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ClassificacaoDatabase.class, "classificacao.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
