package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.stonesdk.sdkdemo.R;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.email.pombo.Contact;
import stone.providers.CancellationProvider;
import stone.providers.PrintProvider;
import stone.providers.SendEmailTransactionProvider;
import stone.utils.PrintObject;
import stone.utils.Stone;

public class TransactionListActivity extends AppCompatActivity implements OnItemClickListener {

    ListView listView;
    List<TransactionObject> transactionObjects;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        listView = findViewById(R.id.listTransactionActivity);

        // acessa todas as transacoes do banco de dados
        TransactionDAO transactionDAO = new TransactionDAO(getApplicationContext());
        // cria uma lista com todas as transacoes
        transactionObjects = transactionDAO.getAllTransactionsOrderByIdDesc();

        // exibe todas as transações (neste caso valor e status) para o usuario
        String[] rowOfList = new String[transactionObjects.size()];
        for (int i = 0; i < transactionObjects.size(); i++) {
            rowOfList[i] = String.format("%s=%s\n%s", transactionObjects.get(i).getIdFromBase(), transactionObjects.get(i).getAmount(), transactionObjects.get(i).getTransactionStatus());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, rowOfList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final TransactionObject selectedTransaction = transactionObjects.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_dialog_title)
                .setMessage(R.string.list_dialog_message)
                .setPositiveButton(R.string.list_dialog_print, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            // lógica da impressão
                            List<PrintObject> listToPrint = new ArrayList<>();
                            for (int i = 0; i < 10; i++) {
                                listToPrint.add(new PrintObject("Teste de impressão linha " + i, PrintObject.MEDIUM, PrintObject.CENTER));
                            }
                            // Stone.getPinpadFromListAt(0) eh o pinpad conectado, que esta na posicao zero.
                            final PrintProvider printProvider = new PrintProvider(TransactionListActivity.this, listToPrint, Stone.getPinpadFromListAt(0));
                            printProvider.useDefaultUI(false);
                            printProvider.setDialogMessage("Imprimindo...");
                            printProvider.setConnectionCallback(new StoneCallbackInterface() {
                                public void onSuccess() {
                                    Toast.makeText(getApplicationContext(), "Impressão realizada com sucesso", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                public void onError() {
                                    Toast.makeText(getApplicationContext(), "Um erro ocorreu durante a impressão", Toast.LENGTH_SHORT).show();
                                }
                            });
                            printProvider.execute();
                        } catch (IndexOutOfBoundsException outException) {
                            Toast.makeText(getApplicationContext(), "Conecte-se a um pinpad.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Houve um erro inesperado. Tente novamente mais tarde.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                })
                .setNeutralButton("Enviar Comprovante", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendEmailTransactionProvider sendEmailProvider = new SendEmailTransactionProvider(TransactionListActivity.this, Stone.getUserModel(0), selectedTransaction);
                        sendEmailProvider.useDefaultUI(false);
                        sendEmailProvider.addTo(new Contact("cliente@gmail.com","Nome do Cliente"));
                        sendEmailProvider.setFrom(new Contact("loja@gmail.com","Nome do Estabelecimento"));
                        sendEmailProvider.setDialogMessage("Enviando comprovante");
                        sendEmailProvider.setConnectionCallback(new StoneCallbackInterface() {
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "Enviado com sucesso", Toast.LENGTH_LONG).show();
                            }

                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Nao enviado", Toast.LENGTH_LONG).show();
                            }
                        });
                        sendEmailProvider.execute();
                    }
                })
                .setNegativeButton(R.string.list_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final CancellationProvider cancellationProvider = new CancellationProvider(TransactionListActivity.this, selectedTransaction.getIdFromBase(), Stone.getUserModel(0));
                        cancellationProvider.useDefaultUI(false); // para dar feedback ao usuario ou nao.
                        cancellationProvider.setDialogMessage("Cancelando...");
                        cancellationProvider.setConnectionCallback(new StoneCallbackInterface() { // chamada de retorno.
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), cancellationProvider.getMessageFromAuthorize(), Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            public void onError() {
                                Toast.makeText(getApplicationContext(), "Um erro ocorreu durante o cancelamento com a transacao de id: " + selectedTransaction.getIdFromBase(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        cancellationProvider.execute();
                    }
                });
        builder.create();
        builder.show();
    }
}
