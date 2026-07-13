package com.diegodev.apidesportes.jogos;


import static com.diegodev.apidesportes.jogos.utils.SharedUtil.salvarHoraRedeSaoPaulo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.adapter.AdpterCat;
import com.diegodev.apidesportes.jogos.adapter.DataAdapter;
import com.diegodev.apidesportes.jogos.adapter.JogosAdapter;
import com.diegodev.apidesportes.jogos.bancoSql.CategoriaDatabase;
import com.diegodev.apidesportes.jogos.bancoSql.JogosDatabase;
import com.diegodev.apidesportes.jogos.item.ItemCat;
import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.response.ApiMoviesCaller;
import com.diegodev.apidesportes.jogos.response.RpCategory;
import com.diegodev.apidesportes.jogos.utils.ApiConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityEsporte extends AppCompatActivity {

    private RecyclerView recyclerViewCate, listView,recyclerViewDatas;
    private AdpterCat adapter;
    private  String url = "";
    private String token;
    private static final String TAG = "EsporteActivity";
    private CategoriaDatabase db;
    private JogosDatabase dbjogos;
    private LinearLayout splash,geral,lisvazia,loading;
    private int tentativas = 0;
    private final int MAX_TENTATIVAS = 7;
    private Handler handler = new Handler(Looper.getMainLooper());
    public static String horaBaseFormatada = "";

    private static final String PREFS_NAME = "ApiEsporteBrPrefs";
    private static final String KEY_TOKEN = "token";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setContentView(R.layout.frame_esportes); // usando o mesmo layout do fragmento

        String token1 = getToken();
        if (!token1.isEmpty()) {
            token = token1;
        }else{
            Toast.makeText(getApplicationContext(), "Token Invalido ou Vazio", Toast.LENGTH_LONG).show();
            finish();
        }

        salvarHoraRedeSaoPaulo(this);

        InicarApi();

        splash = findViewById(R.id.splash);
        geral = findViewById(R.id.lineargeral);
        lisvazia = findViewById(R.id.lisvazia);
        loading = findViewById(R.id.loading);
        recyclerViewDatas = findViewById(R.id.recyclerDatas);
        recyclerViewCate = findViewById(R.id.reciclecategoryjogo);
        recyclerViewCate.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCate.setOverScrollMode(View.OVER_SCROLL_NEVER);
        recyclerViewCate.setHasFixedSize(true);
        splash.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);
        listView = findViewById(R.id.listview);
        listView.setLayoutManager(new LinearLayoutManager(this));

        dbjogos = JogosDatabase.getInstance(this);
        db = CategoriaDatabase.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splash.setVisibility(View.GONE);
            geral.setVisibility(View.VISIBLE);
            new Thread(this::jogosdodia2).start();
            recicleDate();
        }, 4_000);


    }

    public String getToken() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Recupera o valor salvo na chave "token". Se não existir, retorna uma string vazia.
        return prefs.getString("token", "");
    }


    private void InicarApi(){
        url = ApiConfig.getBaseUrl();
        Log.d(TAG, "Base URL selecionada: " + url);

        RpCategory rp = new RpCategory(this);
        rp.execute(url+"campeonatos",token);

        ApiMoviesCaller caller = new ApiMoviesCaller(this);
        caller.chamarApiMovies(url,token);

    }

    private void recicleDate() {

        recyclerViewDatas.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<String> datas = gerarListaDeDatas(this);
        DataAdapter adapter = new DataAdapter(this, datas,this);
        recyclerViewDatas.setAdapter(adapter);
    }


    public static List<String> gerarListaDeDatas(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("ClienteSetup", Context.MODE_PRIVATE);
        String dataBase = prefs.getString("DataAtual", null);

        List<String> listaDatas = new ArrayList<>();

        try {
            if (dataBase != null) {
                SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                sdfFull.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                Date dataAtual = sdfFull.parse(dataBase);
                Log.d("DATA_BASE", "Data base do SharedPreferences: " + dataBase);
                Log.d("DATA_BASE", "Data atual interpretada: " + dataAtual);

                // 🎯 Formatar HH:mm e salvar na variável estática
                SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
                sdfHora.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                horaBaseFormatada = sdfHora.format(dataAtual);
                Log.d("HORA_BASE", "Hora formatada: " + horaBaseFormatada);

                SimpleDateFormat sdfDataSimples = new SimpleDateFormat("dd/MM", Locale.getDefault());
                sdfDataSimples.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"));
                calendar.setTime(dataAtual);
                for (int i = 0; i < 5; i++) {
                    String dataStr = sdfDataSimples.format(calendar.getTime());
                    Log.d("GERAR_DATAS", "Data gerada [" + i + "]: " + dataStr);
                    listaDatas.add(dataStr);

                    calendar.add(Calendar.DAY_OF_MONTH, 1); // só avança após salvar
                }
            } else {
                Log.w("DATA_BASE", "SharedPreferences 'DataAtual' está null");
            }

        } catch (ParseException e) {
            Log.e("DATA_BASE", "Erro ao fazer parse da data", e);
        }

        return listaDatas;
    }




    public void buscarJogosPorData(String data) {
        tentativas = 0;
        tentarBuscarJogos(data);
    }

    private void tentarBuscarJogos(String data) {
        loading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<ItemJogos> jogos = dbjogos.jogosDao().getJogosPorData(data);
            if (jogos != null && !jogos.isEmpty()) {
                runOnUiThread(() -> setList(jogos));


            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("JogosPorData", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(() -> tentarBuscarJogos(data), 1000); // 1 segundo
                } else {
                    runOnUiThread(this::JogosVazio);
                    Log.d("JogosPorData", "Nenhum jogo encontrado para: " + data + " após 5 tentativas.");
                }
            }
        }).start();
    }

    private void JogosVazio(){
        runOnUiThread(() -> {
            loading.setVisibility(View.GONE);
            setList(new ArrayList<>());
            lisvazia.setVisibility(View.VISIBLE);

        });
    }


    public void buscarJogosPorId(int idCamp) {
        tentativas = 0;
        tentarBuscarJogosPorId(idCamp);
    }

    private void tentarBuscarJogosPorId(int idCamp) {
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<ItemJogos> jogosFiltrados = dbjogos.jogosDao().getJogosPorIdCamp(idCamp);
            if (jogosFiltrados != null && !jogosFiltrados.isEmpty()) {
                runOnUiThread(() -> setList(jogosFiltrados));

            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("JogosFiltrados", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(() -> tentarBuscarJogosPorId(idCamp), 1000); // 1 segundo
                } else {
                    runOnUiThread(this::JogosVazio);
                    Log.d("JogosFiltrados", "Nenhum jogo encontrado para o idCamp: " + idCamp + " após 5 tentativas.");
                }
            }
        }).start();
    }


    public void jogosdodia2() {
        tentativas = 0;
        tentarBuscarJogosDoDia();
    }

    private void tentarBuscarJogosDoDia() {
        new Thread(() -> {
            List<ItemCat> jogosFiltrados = db.categoriaDao().getTodas();
            if (jogosFiltrados != null && !jogosFiltrados.isEmpty()) {
                Collections.sort(jogosFiltrados, (a, b) -> a.getCategoryname().compareToIgnoreCase(b.getCategoryname()));
                runOnUiThread(() -> {
                    adapter = new AdpterCat(this, jogosFiltrados, this);
                    recyclerViewCate.setAdapter(adapter);
                });
            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("JogosFiltrados", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(this::tentarBuscarJogosDoDia, 1000); // 1 segundo
                } else {
                    Log.d("JogosFiltrados", "Categoria de Campeonatos Vazia após 5 tentativas.");
                }
            }
        }).start();
    }


    private void setList(List<ItemJogos> itemJogos) {
        Log.d(TAG, "setList: Recebendo lista com " + itemJogos.size() + " itens.");

        if (listView == null) {
            Log.e(TAG, "setList: ListView é null - Adaptador não foi definido");
            return;
        }

        runOnUiThread(() -> {
            lisvazia.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            JogosAdapter myAdapter = new JogosAdapter(this, itemJogos);
            listView.setAdapter(myAdapter);

        });
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        finish();

    }


}



