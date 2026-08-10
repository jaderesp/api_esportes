package com.diegodev.apidesportes.jogos.item;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Linha da tabela de classificação de um campeonato.
 * Vem da rota: GET {baseUrl}campeonato/{id}/classificacao
 *
 * Mapeia o padrão comum de classificação (posicao, time, pontos, jogos, etc.).
 * Se a API retornar nomes de campos diferentes, ajuste os @SerializedName aqui.
 */
@Entity(tableName = "classificacao")
public class ItemClassificacao {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    /** ID do campeonato ao qual esta linha pertence (para filtrar o cache). */
    @ColumnInfo(name = "camp_id")
    private int campId;

    @ColumnInfo(name = "posicao")
    @SerializedName("posicao")
    private int posicao;

    @ColumnInfo(name = "time_name")
    private String timeName;

    @ColumnInfo(name = "logo")
    private String logo;

    @ColumnInfo(name = "pontos")
    @SerializedName("pontos")
    private int pontos;

    @ColumnInfo(name = "jogos")
    @SerializedName("jogos")
    private int jogos;

    @ColumnInfo(name = "vitorias")
    @SerializedName("vitorias")
    private int vitorias;

    @ColumnInfo(name = "empates")
    @SerializedName("empates")
    private int empates;

    @ColumnInfo(name = "derrotas")
    @SerializedName("derrotas")
    private int derrotas;

    @ColumnInfo(name = "gols_pro")
    @SerializedName(value = "gols_pro", alternate = {"golsPro"})
    private int golsPro;

    @ColumnInfo(name = "gols_contra")
    @SerializedName(value = "gols_contra", alternate = {"golsContra"})
    private int golsContra;

    @ColumnInfo(name = "saldo_gols")
    @SerializedName(value = "saldo_gols", alternate = {"saldoGols"})
    private int saldoGols;

    @ColumnInfo(name = "promocao")
    @SerializedName("promocao")
    private String promocao;

    @ColumnInfo(name = "promocao_slug")
    @SerializedName("promocao_slug")
    private String promocaoSlug;

    @ColumnInfo(name = "promocao_cor")
    @SerializedName("promocao_cor")
    private String promocaoCor;

    /** Objeto "time" vindo da API. Não vai para o banco; é achatado em timeName/logo. */
    @Ignore
    @SerializedName("time")
    private Time time;

    // Getters e Setters
    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public int getCampId() { return campId; }
    public void setCampId(int campId) { this.campId = campId; }

    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }

    public String getTimeName() { return timeName; }
    public void setTimeName(String timeName) { this.timeName = timeName; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public int getPontos() { return pontos; }
    public void setPontos(int pontos) { this.pontos = pontos; }

    public int getJogos() { return jogos; }
    public void setJogos(int jogos) { this.jogos = jogos; }

    public int getVitorias() { return vitorias; }
    public void setVitorias(int vitorias) { this.vitorias = vitorias; }

    public int getEmpates() { return empates; }
    public void setEmpates(int empates) { this.empates = empates; }

    public int getDerrotas() { return derrotas; }
    public void setDerrotas(int derrotas) { this.derrotas = derrotas; }

    public int getGolsPro() { return golsPro; }
    public void setGolsPro(int golsPro) { this.golsPro = golsPro; }

    public int getGolsContra() { return golsContra; }
    public void setGolsContra(int golsContra) { this.golsContra = golsContra; }

    public int getSaldoGols() { return saldoGols; }
    public void setSaldoGols(int saldoGols) { this.saldoGols = saldoGols; }

    public String getPromocao() { return promocao; }
    public void setPromocao(String promocao) { this.promocao = promocao; }

    public String getPromocaoSlug() { return promocaoSlug; }
    public void setPromocaoSlug(String promocaoSlug) { this.promocaoSlug = promocaoSlug; }

    public String getPromocaoCor() { return promocaoCor; }
    public void setPromocaoCor(String promocaoCor) { this.promocaoCor = promocaoCor; }

    public Time getTime() { return time; }
    public void setTime(Time time) { this.time = time; }

    /** Objeto "time" do JSON. Nome/logo são achatados pelo ApiClassificacaoCaller. */
    public static class Time {
        @SerializedName(value = "nome", alternate = {"nome_popular", "name"})
        private String nome;

        @SerializedName(value = "logo", alternate = {"escudo", "url_logo", "logo_url"})
        private String logo;

        public String getNome() { return nome; }
        public String getLogo() { return logo; }
    }
}
