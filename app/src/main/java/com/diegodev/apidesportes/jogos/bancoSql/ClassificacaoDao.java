package com.diegodev.apidesportes.jogos.bancoSql;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.diegodev.apidesportes.jogos.item.ItemClassificacao;

import java.util.List;

@Dao
public interface ClassificacaoDao {

    @Insert
    void insertAll(List<ItemClassificacao> itens);

    @Query("DELETE FROM classificacao WHERE camp_id = :campId")
    void limparPorCamp(int campId);

    @Query("DELETE FROM classificacao")
    void limpar();

    /** Tabela de classificação de um campeonato, ordenada por posição. */
    @Query("SELECT * FROM classificacao WHERE camp_id = :campId ORDER BY posicao ASC")
    List<ItemClassificacao> getPorCamp(int campId);
}
