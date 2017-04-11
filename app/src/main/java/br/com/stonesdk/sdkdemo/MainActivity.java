package br.com.stonesdk.sdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import stone.application.interfaces.StoneCallbackInterface;
import stone.cache.ApplicationCache;
import stone.providers.DownloadTablesProvider;
import stone.utils.GlobalInformations;

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
                "Envio de e-mail"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int itemPosition = position;

                // Para cada nova opção na lista, um novo "case" precisa ser inserido aqui.
                switch (itemPosition) {
                    case 0:
                        Intent devicesIntent = new Intent(MainActivity.this, DevicesActivity.class);
                        MainActivity.this.startActivity(devicesIntent);
                        break;
                    case 1:
                        // Verifica se o bluetooth esta ligado e se existe algum pinpad conectado.
                        if (GlobalInformations.getPinpadListSize() != null && GlobalInformations.getPinpadListSize() > 0) {
                            Intent transactionIntent = new Intent(MainActivity.this, TransactionActivity.class);
                            MainActivity.this.startActivity(transactionIntent);
                            break;
                        } else {
                            Toast.makeText(getApplicationContext(), "Conecte-se a um pinpad.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    case 2:
                        Intent transactionListIntent = new Intent(MainActivity.this, TransactionListActivity.class);
                        MainActivity.this.startActivity(transactionListIntent);
                        break;
                    case 3:
                        Intent sendEmailIntent = new Intent(MainActivity.this, SendEmailActivity.class);
                        MainActivity.this.startActivity(sendEmailIntent);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
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
