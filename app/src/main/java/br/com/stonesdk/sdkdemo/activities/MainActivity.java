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
import android.widget.Toast;

import br.com.stonesdk.sdkdemo.R;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.ActiveApplicationProvider;
import stone.providers.DisplayMessageProvider;
import stone.providers.ReversalProvider;
import stone.utils.Stone;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

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
                "Mostrar Mensagem no pinpad",
                "Cancelar transações com erro",
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
            case 4:
                final ReversalProvider reversalProvider = new ReversalProvider(this);
                reversalProvider.useDefaultUI(true);
                reversalProvider.setDialogMessage("Cancelando transações com erro");
                reversalProvider.setConnectionCallback(new StoneCallbackInterface() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Transações canceladas com sucesso", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(MainActivity.this, "Ocorreu um erro durante o cancelamento das tabelas: " + reversalProvider.getListOfErrors(), Toast.LENGTH_SHORT).show();
                    }
                });
                reversalProvider.execute();
                break;
            case 5:
                final ActiveApplicationProvider provider = new ActiveApplicationProvider(MainActivity.this);
                provider.setDialogMessage("Desativando o aplicativo...");
                provider.setDialogTitle("Aguarde");
                provider.useDefaultUI(true);
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
                if (Stone.getPinpadListSize() > 0) {
                    Intent closeBluetoothConnectionIntent = new Intent(MainActivity.this, DisconnectPinpadActivity.class);
                    startActivity(closeBluetoothConnectionIntent);
                } else {
                    Toast.makeText(this, "Nenhum device Conectado", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
