package br.com.stonesdk.sdkdemo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import br.com.stonesdk.sdkdemo.R;
import stone.application.interfaces.StoneCallbackInterface;
import stone.cache.ApplicationCache;
import stone.providers.ActiveApplicationProvider;
import stone.providers.DownloadTablesProvider;
import stone.providers.LoadTablesProvider;
import stone.providers.commands.gcr.GcrRequestCommand;
import stone.utils.GlobalInformations;
import stone.utils.Stone;

public class MainActivity extends AppCompatActivity {

    ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listMainActivity);

        String[] options = new String[]{
                "Dispositivos pareados",
                "Fazer uma transação",
                "Listar transações",
                "Envio de e-mail",
                "Atualizar Tabelas",
                "Desativar"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;

                // Para cada nova opção na lista, um novo "case" precisa ser inserido aqui.
                switch (itemPosition) {
                    case 0:
                        Intent devicesIntent = new Intent(MainActivity.this, DevicesActivity.class);
                        startActivity(devicesIntent);
                        break;
                    case 1:
                        // Verifica se o bluetooth esta ligado e se existe algum pinpad conectado.
                        if (Stone.getPinpadListSize() != null && Stone.getPinpadListSize() > 0) {
                            Intent transactionIntent = new Intent(MainActivity.this, TransactionActivity.class);
                            startActivity(transactionIntent);
                            break;
                        } else {
                            Toast.makeText(getApplicationContext(), "Conecte-se a um pinpad.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    case 2:
                        Intent transactionListIntent = new Intent(MainActivity.this, TransactionListActivity.class);
                        startActivity(transactionListIntent);
                        break;
                    case 3:
                        Intent sendEmailIntent = new Intent(MainActivity.this, SendEmailActivity.class);
                        startActivity(sendEmailIntent);
                        break;
                    case 4:
                        if (Stone.getPinpadListSize() == null || Stone.getPinpadListSize() <= 0) {
                            Toast.makeText(getApplicationContext(), "Conecte-se a um pinpad.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        GcrRequestCommand gcrRequestCommand = new GcrRequestCommand();
                        gcrRequestCommand.setAcqidxreq("16");
                        LoadTablesProvider loadTablesProvider = new LoadTablesProvider(MainActivity.this, gcrRequestCommand, GlobalInformations.getPinpadFromListAt(0));
                        loadTablesProvider.setDialogMessage("Subindo as tabelas");
                        loadTablesProvider.setWorkInBackground(false); // para dar feedback ao usuario ou nao.
                        loadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "Sucesso.", Toast.LENGTH_SHORT).show();
                            }

                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Erro.", Toast.LENGTH_SHORT).show();
                            }
                        });
                        loadTablesProvider.execute();
                        break;
                    case 5:
                        final ActiveApplicationProvider provider = new ActiveApplicationProvider(MainActivity.this);
                        provider.setDialogMessage("Desativando o aplicativo...");
                        provider.setDialogTitle("Aguarde");
                        provider.setWorkInBackground(false);
                        provider.setConnectionCallback(new StoneCallbackInterface() {
                            /* Metodo chamado se for executado sem erros */
                            public void onSuccess() {
                                Intent mainIntent = new Intent(MainActivity.this, ValidationActivity.class);
                                startActivity(mainIntent);
                                finish();
                            }

                            /* metodo chamado caso ocorra alguma excecao */
                            public void onError() {
                                Toast.makeText(MainActivity.this, "Erro na ativacao do aplicativo, verifique a lista de erros do provider", Toast.LENGTH_SHORT).show();
                                /* Chame o metodo abaixo para verificar a lista de erros. Para mais detalhes, leia a documentacao: */
                                Log.e("MainActivity", "onError: " + provider.getListOfErrors().toString());

                            }
                        });
                        provider.deactivate();
                    default:
                        break;
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();

//        if (GlobalInformations.isDeveloper() == true) {
//            Toast.makeText(getApplicationContext(), "Modo desenvolvedor", 1).show();
//        }

        // IMPORTANTE: Mantenha esse provider na sua MAIN, pois ele ira baixar as
        // tabelas AIDs e CAPKs dos servidores da Stone e sera utilizada quando necessário.
        ApplicationCache applicationCache = new ApplicationCache(getApplicationContext());
        if (!applicationCache.checkIfHasTables()) {

            // Realiza processo de download das tabelas em sua totalidade.
            DownloadTablesProvider downloadTablesProvider = new DownloadTablesProvider(MainActivity.this, GlobalInformations.getUserModel(0));
            downloadTablesProvider.setDialogMessage("Baixando as tabelas, por favor aguarde");
            downloadTablesProvider.setWorkInBackground(false); // para dar feedback ao usuario ou nao.
            downloadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Tabelas baixadas com sucesso", Toast.LENGTH_SHORT).show();
                }

                public void onError() {
                    Toast.makeText(getApplicationContext(), "Erro no download das tabelas", Toast.LENGTH_SHORT).show();
                }
            });
            downloadTablesProvider.execute();
        }
    }
}
