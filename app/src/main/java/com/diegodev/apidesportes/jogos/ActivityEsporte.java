package com.diegodev.apidesportes.jogos;


import static com.diegodev.apidesportes.jogos.utils.SharedUtil.salvarHoraRedeSaoPaulo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diegodev.apidesportes.R;
import com.diegodev.apidesportes.jogos.adapter.AdpterCat;
import com.diegodev.apidesportes.jogos.adapter.ClassificacaoAdapter;
import com.diegodev.apidesportes.jogos.adapter.DataAdapter;
import com.diegodev.apidesportes.jogos.adapter.JogosAdapter;
import com.diegodev.apidesportes.jogos.dialog.CanaisDialogFragment;
import com.diegodev.apidesportes.jogos.dialog.CanalDetalheDialogFragment;
import com.diegodev.apidesportes.jogos.event.EsporteEventListener;
import com.diegodev.apidesportes.jogos.bancoSql.CategoriaDatabase;
import com.diegodev.apidesportes.jogos.bancoSql.ClassificacaoDatabase;
import com.diegodev.apidesportes.jogos.bancoSql.JogosDatabase;
import com.diegodev.apidesportes.jogos.item.DataItem;
import com.diegodev.apidesportes.jogos.item.ItemCanalLink;
import com.diegodev.apidesportes.jogos.item.ItemCat;
import com.diegodev.apidesportes.jogos.item.ItemClassificacao;
import com.diegodev.apidesportes.jogos.item.ItemJogos;
import com.diegodev.apidesportes.jogos.response.ApiClassificacaoCaller;
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
    private ClassificacaoDatabase dbClassificacao;
    private LinearLayout splash,geral,lisvazia,loading;
    private int tentativas = 0;
    private int tentativasCategorias = 0;
    // Token da busca atual: cada nova busca (data/campeonato/classificação)
    // incrementa este contador. Loops de retry de buscas ANTIGAS são ignorados
    // quando o token mudou — evita que uma busca velha limpe/sobrescreva a lista
    // recém-exibida (ex.: clicar em um campeonato e o auto-load de HOJE limpar tudo).
    private int idBusca = 0;
    private final int MAX_TENTATIVAS = 7;
    private Handler handler = new Handler(Looper.getMainLooper());
    public static String horaBaseFormatada = "";

    // Campeonato selecionado (-1 = nenhum; então não há opção "Tabela" na coluna de datas)
    private int campSelecionadoId = -1;

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
        dbClassificacao = ClassificacaoDatabase.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splash.setVisibility(View.GONE);
            geral.setVisibility(View.VISIBLE);
            new Thread(this::jogosdodia2).start();
            recicleDate();
            autoCarregarHoje();
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

        List<DataItem> itens = new ArrayList<>();
        List<String> datas = gerarListaDeDatas(this);

        // Com um campeonato selecionado, a opção "Tabela" (classificação)
        // fica ACIMA de "HOJE" na coluna lateral.
        if (campSelecionadoId != -1) {
            itens.add(DataItem.classificacao());
        }

        for (int i = 0; i < datas.size(); i++) {
            itens.add(DataItem.data(datas.get(i), i == 0));
        }

        DataAdapter adapter = new DataAdapter(this, itens, this);
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




    /** Carrega os jogos de "HOJE" uma única vez ao abrir a tela (auto-load). */
    private void autoCarregarHoje() {
        List<String> datas = gerarListaDeDatas(this);
        if (!datas.isEmpty()) {
            buscarJogosPorData(datas.get(0));
        }
    }

    public void buscarJogosPorData(String data) {
        tentativas = 0;
        int minhaBusca = ++idBusca;
        tentarBuscarJogos(data, minhaBusca);
    }

    private void tentarBuscarJogos(String data, int minhaBusca) {
        // Esta execução pertence a uma busca antiga que foi substituída.
        if (minhaBusca != idBusca) {
            return;
        }
        loading.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<ItemJogos> jogos = dbjogos.jogosDao().getJogosPorData(data);
            if (minhaBusca != idBusca) {
                return;
            }
            if (jogos != null && !jogos.isEmpty()) {
                runOnUiThread(() -> {
                    if (minhaBusca == idBusca) {
                        setList(jogos);
                    }
                });

            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("JogosPorData", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(() -> tentarBuscarJogos(data, minhaBusca), 1000); // 1 segundo
                } else {
                    runOnUiThread(() -> {
                        if (minhaBusca == idBusca) {
                            JogosVazio();
                        }
                    });
                    Log.d("JogosPorData", "Nenhum jogo encontrado para: " + data + " após 5 tentativas.");
                }
            }
        }).start();
    }

    private void JogosVazio(){
        runOnUiThread(() -> {
            loading.setVisibility(View.GONE);
            setList(new ArrayList<>());
            TextView tvEmpty = findViewById(R.id.tvEmptyMsg);
            if (tvEmpty != null) {
                tvEmpty.setText(R.string.empty_jogos);
            }
            lisvazia.setVisibility(View.VISIBLE);

        });
    }


    public void buscarJogosPorId(int idCamp) {
        // Marca o campeonato selecionado (faz aparecer a opção "Tabela" acima de HOJE)
        campSelecionadoId = idCamp;
        if (adapter != null) {
            adapter.setCampanhaSelecionada(idCamp);
        }
        recicleDate();
        tentativas = 0;
        int minhaBusca = ++idBusca;
        tentarBuscarJogosPorId(idCamp, minhaBusca);
    }

    /** Clique na opção "Tabela": busca e mostra a classificação do campeonato selecionado. */
    public void buscarClassificacao() {
        if (campSelecionadoId == -1) {
            return;
        }
        loading.setVisibility(View.VISIBLE);

        // Limpa o cache antigo do campeonato ANTES de buscar, para a tela não
        // exibir linhas velhas (ex.: sem nome) enquanto a API não responde.
        // Só depois de limpar é que disparamos a API e o polling.
        new Thread(() -> {
            dbClassificacao.classificacaoDao().limparPorCamp(campSelecionadoId);

            runOnUiThread(() -> {
                ApiClassificacaoCaller caller = new ApiClassificacaoCaller(this);
                caller.chamarApi(url, token, campSelecionadoId);

                tentativas = 0;
                int minhaBusca = ++idBusca;
                tentarBuscarClassificacao(minhaBusca);
            });
        }).start();
    }

    private void tentarBuscarClassificacao(int minhaBusca) {
        if (minhaBusca != idBusca) {
            return;
        }
        new Thread(() -> {
            List<ItemClassificacao> lista = dbClassificacao.classificacaoDao().getPorCamp(campSelecionadoId);
            if (minhaBusca != idBusca) {
                return;
            }
            if (lista != null && !lista.isEmpty()) {
                runOnUiThread(() -> {
                    if (minhaBusca == idBusca) {
                        setClassificacao(lista);
                    }
                });
            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("Classificacao", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(() -> tentarBuscarClassificacao(minhaBusca), 1000);
                } else {
                    runOnUiThread(() -> {
                        if (minhaBusca == idBusca) {
                            ClassificacaoVazia();
                        }
                    });
                    Log.d("Classificacao", "Nenhuma classificação encontrada após " + MAX_TENTATIVAS + " tentativas.");
                }
            }
        }).start();
    }

    private void setClassificacao(List<ItemClassificacao> lista) {
        runOnUiThread(() -> {
            lisvazia.setVisibility(View.GONE);
            loading.setVisibility(View.GONE);
            ClassificacaoAdapter adapter = new ClassificacaoAdapter(this, lista);
            listView.setAdapter(adapter);
        });
    }

    private void ClassificacaoVazia() {
        runOnUiThread(() -> {
            loading.setVisibility(View.GONE);
            listView.setAdapter(null);
            TextView tvEmpty = findViewById(R.id.tvEmptyMsg);
            if (tvEmpty != null) {
                tvEmpty.setText(R.string.empty_classificacao);
            }
            lisvazia.setVisibility(View.VISIBLE);
        });
    }

    private void tentarBuscarJogosPorId(int idCamp, int minhaBusca) {
        if (minhaBusca != idBusca) {
            return;
        }
        loading.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<ItemJogos> jogosFiltrados = dbjogos.jogosDao().getJogosPorIdCamp(idCamp);
            if (minhaBusca != idBusca) {
                return;
            }
            if (jogosFiltrados != null && !jogosFiltrados.isEmpty()) {
                runOnUiThread(() -> {
                    if (minhaBusca == idBusca) {
                        setList(jogosFiltrados);
                    }
                });

            } else {
                tentativas++;
                if (tentativas < MAX_TENTATIVAS) {
                    Log.d("JogosFiltrados", "Tentativa " + tentativas + " falhou. Tentando novamente em 1s...");
                    handler.postDelayed(() -> tentarBuscarJogosPorId(idCamp, minhaBusca), 1000); // 1 segundo
                } else {
                    runOnUiThread(() -> {
                        if (minhaBusca == idBusca) {
                            JogosVazio();
                        }
                    });
                    Log.d("JogosFiltrados", "Nenhum jogo encontrado para o idCamp: " + idCamp + " após 5 tentativas.");
                }
            }
        }).start();
    }


    public void jogosdodia2() {
        tentativasCategorias = 0;
        tentarBuscarJogosDoDia();
    }

    private void tentarBuscarJogosDoDia() {
        new Thread(() -> {
            List<ItemCat> jogosFiltrados = db.categoriaDao().getTodas();
            if (jogosFiltrados != null && !jogosFiltrados.isEmpty()) {
                Collections.sort(jogosFiltrados, (a, b) -> a.getCategoryname().compareToIgnoreCase(b.getCategoryname()));
                runOnUiThread(() -> {
                    adapter = new AdpterCat(this, jogosFiltrados, this);
                    adapter.setCampanhaSelecionada(campSelecionadoId);
                    recyclerViewCate.setAdapter(adapter);
                });
            } else {
                tentativasCategorias++;
                if (tentativasCategorias < MAX_TENTATIVAS) {
                    Log.d("JogosFiltrados", "Tentativa " + tentativasCategorias + " falhou. Tentando novamente em 1s...");
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
            // Ao clicar em um jogo, abre o modal (bottom sheet) com os canais de transmissão
            // e notifica o app consumidor (EsporteEventListener) se houver listener registrado.
            myAdapter.setOnItemClickListener(jogo -> {
                CanaisDialogFragment.newInstance(jogo).show(getSupportFragmentManager(), "canais_dialog");
                EsporteEventListener.notificarJogoClicado(jogo);
            });
            // Ao clicar em um canal (canais_links) da linha do jogo, abre o modal de detalhes.
            myAdapter.setOnCanalClickListener((jogo, canal) ->
                    CanalDetalheDialogFragment.newInstance(canal)
                            .show(getSupportFragmentManager(), "canal_detalhe"));
            listView.setAdapter(myAdapter);
            configurarNavegacaoListaJogos();

        });
    }

    /**
     * Mantém o foco D-pad dentro da lista de jogos durante o scroll rápido
     * (key-repeat ao segurar a seta para baixo/cima). Sem isso, quando o item
     * focado sai do viewport o Android entrega o foco à coluna de datas ou à
     * barra de campeonatos, "desfocando" a lista.
     */
    private void configurarNavegacaoListaJogos() {
        listView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (keyCode != KeyEvent.KEYCODE_DPAD_DOWN && keyCode != KeyEvent.KEYCODE_DPAD_UP) {
                return false;
            }
            return navegarListaJogos(keyCode);
        });
    }

    /**
     * Intercepta o botão para baixo/cima quando o foco está na lista de jogos,
     * mesmo durante auto-repeat (scroll rápido). Garante que o foco navegue
     * item a item e nunca saia da lista: no fim permanece no último jogo.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int code = event.getKeyCode();
            if ((code == KeyEvent.KEYCODE_DPAD_DOWN || code == KeyEvent.KEYCODE_DPAD_UP)
                    && listView != null
                    && listView.getAdapter() != null
                    && listView.getAdapter().getItemCount() > 0) {
                View focus = getCurrentFocus();
                if (focus != null && estaDentroDaLista(focus)) {
                    if (navegarListaJogos(code)) {
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /** Verdadeiro se o view está dentro da lista de jogos (ou é a própria lista). */
    private boolean estaDentroDaLista(View view) {
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent == listView) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private boolean navegarListaJogos(int keyCode) {
        LinearLayoutManager lm = (LinearLayoutManager) listView.getLayoutManager();
        RecyclerView.Adapter<?> adapter = listView.getAdapter();
        if (lm == null || adapter == null || adapter.getItemCount() == 0) {
            return false;
        }

        View focused = listView.getFocusedChild();
        int pos = focused != null
                ? listView.getChildAdapterPosition(focused)
                : lm.findFirstVisibleItemPosition();
        if (pos < 0) {
            pos = lm.findFirstVisibleItemPosition();
        }

        int delta = (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) ? 1 : -1;
        int alvo = pos + delta;
        int count = adapter.getItemCount();

        // Salta itens não focáveis (cabeçalho fixo e barras de zona de promoção da
        // classificação) para o foco sempre parar em uma linha de time. Sem isso, a
        // barra entre dois blocos (ex.: Libertadores/Sudamericana) "engole" o foco.
        if (adapter instanceof ClassificacaoAdapter) {
            ClassificacaoAdapter classificacao = (ClassificacaoAdapter) adapter;
            while (alvo >= 0 && alvo < count && !classificacao.isFocavel(alvo)) {
                alvo += delta;
            }
        }

        // No topo, pressionar para cima deixa o foco sair da lista
        // (navegação intencional para a barra de campeonatos/acima).
        if (alvo < 0) {
            return false;
        }

        // No fim, pressionar para baixo permanece no último jogo.
        if (alvo >= count) {
            alvo = count - 1;
        }

        View childAlvo = lm.findViewByPosition(alvo);
        if (childAlvo != null) {
            childAlvo.requestFocus();
            return true;
        }

        // Item fora do viewport: rola e só então devolve o foco ao item,
        // aguardando o layout terminar para o item existir na hierarquia.
        lm.scrollToPosition(alvo);
        focarItemAposLayout(alvo);
        return true;
    }

    /**
     * Rola a lista até {@code posicao} e pede foco ao item assim que ele
     * estiver desenhado. Usado quando o alvo está fora do viewport: um simples
     * post() pode rodar antes do layout, deixando o item ainda inexistente.
     */
    private void focarItemAposLayout(int posicao) {
        listView.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                RecyclerView.LayoutManager lm = listView.getLayoutManager();
                View child = lm != null ? lm.findViewByPosition(posicao) : null;
                if (child != null) {
                    listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    child.requestFocus();
                }
            }
        });
        // Garantia: dispara um layout para o listener ser chamado.
        listView.requestLayout();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        finish();

    }


}



