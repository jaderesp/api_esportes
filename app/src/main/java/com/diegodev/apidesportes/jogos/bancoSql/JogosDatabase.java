package com.diegodev.apidesportes.jogos.bancoSql;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.diegodev.apidesportes.jogos.item.ItemJogos;


// version = 2 porque adicionamos os campos de canais na tabela "jogos".
// fallbackToDestructiveMigration() apaga o cache antigo automaticamente na atualização.
@Database(entities = {ItemJogos.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class JogosDatabase extends RoomDatabase {

    private static volatile JogosDatabase INSTANCE;

    public abstract JogosDao jogosDao();

    public static JogosDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (JogosDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    JogosDatabase.class, "jogos.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
