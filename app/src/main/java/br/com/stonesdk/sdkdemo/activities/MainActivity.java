package br.com.stonesdk.sdkdemo.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import br.com.stonesdk.sdkdemo.R;
import stone.application.interfaces.StoneCallbackInterface;
import stone.cache.ApplicationCache;
import stone.providers.ActiveApplicationProvider;
import stone.providers.DisplayMessageProvider;
import stone.providers.DownloadTablesProvider;
import stone.providers.LoadTablesProvider;
import stone.utils.Stone;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;
import static stone.utils.Stone.getPinpadFromListAt;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    ListView listView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listMainActivity);

        String[] options = new String[]{
                "Dispositivos pareados",
                "Fazer uma transação",
                "Listar transações",
                "Atualizar Tabelas",
                "Mostrar Mensagem no pinpad",
                "Desativar",
                "Desconectar com um pinpad"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, options);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        // Para cada nova opção na lista, um novo "case" precisa ser inserido aqui.
        switch (position) {
            case 0:
                Intent devicesIntent = new Intent(MainActivity.this, DevicesActivity.class);
                startActivity(devicesIntent);
                break;
            case 1:
                // Verifica se o bluetooth esta ligado e se existe algum pinpad conectado.
                if (Stone.getPinpadListSize() > 0) {
                    Intent transactionIntent = new Intent(MainActivity.this, TransactionActivity.class);
                    startActivity(transactionIntent);
                    break;
                } else {
                    makeText(getApplicationContext(), "Conecte-se a um pinpad.", LENGTH_SHORT).show();
                    break;
                }
            case 2:
                Intent transactionListIntent = new Intent(MainActivity.this, TransactionListActivity.class);
                startActivity(transactionListIntent);
                break;
            case 3:
                if (Stone.getPinpadListSize() <= 0) {
                    makeText(getApplicationContext(), "Conecte-se a um pinpad.", LENGTH_SHORT).show();
                    break;
                }
                LoadTablesProvider loadTablesProvider = new LoadTablesProvider(MainActivity.this, getPinpadFromListAt(0), Stone.getUserModel(0));
                loadTablesProvider.setDialogMessage("Subindo as tabelas");
                loadTablesProvider.useDefaultUI(false); // para dar feedback ao usuario ou nao.
                loadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                    public void onSuccess() {
                        makeText(getApplicationContext(), "Sucesso.", LENGTH_SHORT).show();
                    }

                    public void onError() {
                        makeText(getApplicationContext(), "Erro.", LENGTH_SHORT).show();
                    }
                });
                loadTablesProvider.execute();
                break;
            case 4:
                if (Stone.getPinpadListSize() <= 0) {
                    makeText(getApplicationContext(), "Conecte-se a um pinpad.", LENGTH_SHORT).show();
                    break;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Digite a mensagem para mostrar no pinpad");
                final EditText editText = new EditText(MainActivity.this);
                builder.setView(editText);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        DisplayMessageProvider displayMessageProvider =
                                new DisplayMessageProvider(MainActivity.this, text, Stone.getPinpadFromListAt(0));
                        displayMessageProvider.execute();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            case 5:
                final ActiveApplicationProvider provider = new ActiveApplicationProvider(MainActivity.this);
                provider.setDialogMessage("Desativando o aplicativo...");
                provider.setDialogTitle("Aguarde");
                provider.useDefaultUI(false);
                provider.setConnectionCallback(new StoneCallbackInterface() {
                    /* Metodo chamado se for executado sem erros */
                    public void onSuccess() {
                        Intent mainIntent = new Intent(MainActivity.this, ValidationActivity.class);
                        startActivity(mainIntent);
                        finish();
                    }

                    /* metodo chamado caso ocorra alguma excecao */
                    public void onError() {
                        makeText(MainActivity.this, "Erro na ativacao do aplicativo, verifique a lista de erros do provider", LENGTH_SHORT).show();

            /* Chame o metodo abaixo para verificar a lista de erros. Para mais detalhes, leia a documentacao: */
                        Log.e("MainActivity", "onError: " + provider.getListOfErrors().toString());
                    }
                });
                provider.deactivate();
            case 6:
                Intent closeBluetoothConnectionIntent = new Intent(MainActivity.this, DisconnectPinpadActivity.class);
                startActivity(closeBluetoothConnectionIntent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // IMPORTANTE: Mantenha esse provider na sua MAIN, pois ele ira baixar as
        // tabelas AIDs e CAPKs dos servidores da Stone e sera utilizada quando necessário.
        ApplicationCache applicationCache = new ApplicationCache(getApplicationContext());
        if (!applicationCache.checkIfHasTables(Stone.getUserModel(0).getTableVersion())) {

            // Realiza processo de download das tabelas em sua totalidade.
            DownloadTablesProvider downloadTablesProvider = new DownloadTablesProvider(MainActivity.this, Stone.getUserModel(0));
            downloadTablesProvider.setDialogMessage("Baixando as tabelas, por favor aguarde");
            downloadTablesProvider.useDefaultUI(false); // para dar feedback ao usuario ou nao.
            downloadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                public void onSuccess() {
                    makeText(getApplicationContext(), "Tabelas baixadas com sucesso",
                            LENGTH_SHORT).show();
                }

                public void onError() {
                    makeText(getApplicationContext(), "Erro no download das tabelas",
                            LENGTH_SHORT).show();
                }
            });
            downloadTablesProvider.execute();
        }
    }
}
