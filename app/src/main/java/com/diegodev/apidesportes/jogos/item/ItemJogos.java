package com.diegodev.apidesportes.jogos.item;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;


@Entity(tableName = "jogos")
public class ItemJogos {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @ColumnInfo(name = "time_a")
    private String timeA;

    @ColumnInfo(name = "id_camp")
    private int idCamp;

    @ColumnInfo(name = "logo_a")
    private String logoA;

    @ColumnInfo(name = "gols_a")
    private int golsA;

    @ColumnInfo(name = "time_b")
    private String timeB;

    @ColumnInfo(name = "logo_b")
    private String logoB;

    @ColumnInfo(name = "gols_b")
    private int golsB;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "start")
    private String start;

    // ✅ Novos campos para o campeonato
    @ColumnInfo(name = "camp_name")
    private String campName;

    @ColumnInfo(name = "logo_camp")
    private String logoCamp;

    @ColumnInfo(name = "camp_id")
    private int CampId;

    // ✅ Canais de transmissão (novos campos da API /api/jogos)
    // Arrays de nomes de canais. O Room salva como JSON (ver Converters.java).
    @ColumnInfo(name = "canais")
    private List<String> canais;

    @ColumnInfo(name = "canais_ia")
    private List<String> canaisIa;

    @ColumnInfo(name = "canais_simples")
    private List<String> canaisSimples;

    @ColumnInfo(name = "canais_links")
    private List<String> canaisLinks;

    @Ignore
    @SerializedName("campeonato")
    private Campeonato campeonato;

    // Getters e Setters
    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public String getTimeA() { return timeA; }
    public void setTimeA(String timeA) { this.timeA = timeA; }

    public int getIdCamp() { return idCamp; }
    public void setIdCamp(int idCamp) { this.idCamp = idCamp; }

    public String getLogoA() { return logoA; }
    public void setLogoA(String logoA) { this.logoA = logoA; }

    public int getGolsA() { return golsA; }
    public void setGolsA(int golsA) { this.golsA = golsA; }

    public String getTimeB() { return timeB; }
    public void setTimeB(String timeB) { this.timeB = timeB; }

    public String getLogoB() { return logoB; }
    public void setLogoB(String logoB) { this.logoB = logoB; }

    public int getGolsB() { return golsB; }
    public void setGolsB(int golsB) { this.golsB = golsB; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStart() { return start; }
    public void setStart(String start) { this.start = start; }

    public String getCampName() { return campName; }
    public void setCampName(String campName) { this.campName = campName; }

    public String getLogoCamp() { return logoCamp; }
    public void setLogoCamp(String logoCamp) { this.logoCamp = logoCamp; }

    public int getCampId() { return CampId ; }
    public void setCampId(int CampId) { this.CampId = CampId; }

    public List<String> getCanais() { return canais; }
    public void setCanais(List<String> canais) { this.canais = canais; }

    public List<String> getCanaisIa() { return canaisIa; }
    public void setCanaisIa(List<String> canaisIa) { this.canaisIa = canaisIa; }

    public List<String> getCanaisSimples() { return canaisSimples; }
    public void setCanaisSimples(List<String> canaisSimples) { this.canaisSimples = canaisSimples; }

    public List<String> getCanaisLinks() { return canaisLinks; }
    public void setCanaisLinks(List<String> canaisLinks) { this.canaisLinks = canaisLinks; }

    public Campeonato getCampeonato() { return campeonato; }
    public void setCampeonato(Campeonato campeonato) { this.campeonato = campeonato; }

    // Classe interna para o JSON da API
    public static class Campeonato {
        @SerializedName("campName")
        private String campName;

        @SerializedName("logoCamp")
        private String logoCamp;

        @SerializedName("id")
        private int CampeonatoId;

        public String getCampName() {
            return campName;
        }

        public String getLogoCamp() {
            return logoCamp;
        }

        public int getCampId() {
            return CampeonatoId;
        }
    }
}
