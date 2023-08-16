package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.stone.posandroid.providers.PosPrintReceiptProvider;
import br.com.stonesdk.sdkdemo.R;
import br.com.stonesdk.sdkdemo.controller.PrintController;
import stone.application.enums.ReceiptType;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionDAO;
import stone.database.transaction.TransactionObject;
import stone.providers.CancellationProvider;
import stone.providers.CaptureTransactionProvider;
import stone.providers.PrintProvider;
import stone.providers.SendEmailTransactionProvider;
import stone.repository.remote.email.pombo.email.Contact;
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
        ArrayList<String> optionsList = new ArrayList<String>() {{
            add("[Pinpad] Imprimir comprovante");
            add("[POS] Imprimir via do estabelecimento");
            add("[POS] Imprimir via do cliente");
            add("[POS] Imprimir comprovante customizado");
            add("Cancelar");
            add("Enviar via do cliente");
            add("Enviar via do estabelecimento");
        }};
        if (!selectedTransaction.isCapture()) {
            optionsList.add("Capturar Transação");
        }
        String[] options = new String[optionsList.size()];
        optionsList.toArray(options);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_dialog_title)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case 0:
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
                                break;

                            case 1:
                                printReceipt(ReceiptType.MERCHANT, selectedTransaction);
                                break;

                            case 2:
                                printReceipt(ReceiptType.CLIENT, selectedTransaction);
                                break;

                            case 3:
                                // Impressão customizada
                                final PosPrintProvider customPosPrintProvider = new PosPrintProvider(TransactionListActivity.this);
                                customPosPrintProvider.addLine("Stone");
                                customPosPrintProvider.addLine("Comprovante customizado");
                                customPosPrintProvider.addLine("");
                                customPosPrintProvider.addLine("PAN : " + selectedTransaction.getCardHolderNumber());
                                customPosPrintProvider.addLine("DATE/TIME : " + selectedTransaction.getDate() + " " + selectedTransaction.getTime());
                                customPosPrintProvider.addLine("AMOUNT : " + selectedTransaction.getAmount());
                                customPosPrintProvider.addLine("ATK : " + selectedTransaction.getAcquirerTransactionKey());
                                customPosPrintProvider.addLine("");
                                customPosPrintProvider.addLine("Signature");
//                                customPosPrintProvider.addBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.signature));
                                customPosPrintProvider.setConnectionCallback(new StoneCallbackInterface() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(TransactionListActivity.this, "Recibo impresso", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError() {
                                        Toast.makeText(TransactionListActivity.this, "Erro ao imprimir: " + customPosPrintProvider.getListOfErrors(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                customPosPrintProvider.execute();
                                break;

                            case 4:
                                final CancellationProvider cancellationProvider = new CancellationProvider(TransactionListActivity.this, selectedTransaction);
                                cancellationProvider.useDefaultUI(false); // para dar feedback ao usuario ou nao.
                                cancellationProvider.setDialogMessage("Cancelando...");
                                cancellationProvider.setConnectionCallback(new StoneCallbackInterface() { // chamada de retorno.
                                    public void onSuccess() {
                                        Toast.makeText(getApplicationContext(),
                                                cancellationProvider.getMessageFromAuthorize(),
                                                Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                    public void onError() {
                                        Toast.makeText(getApplicationContext(), "Um erro ocorreu durante o cancelamento com a transacao de id: " + selectedTransaction.getIdFromBase(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                cancellationProvider.execute();
                                break;
                            case 5:
                                sendReceipt(selectedTransaction, ReceiptType.CLIENT);
                                break;
                            case 6:
                                sendReceipt(selectedTransaction, ReceiptType.MERCHANT);
                                break;
                            case 7:
                                final CaptureTransactionProvider provider = new CaptureTransactionProvider(TransactionListActivity.this, selectedTransaction);
                                provider.useDefaultUI(true);
                                provider.setDialogMessage("Efetuando Captura...");
                                provider.setConnectionCallback(new StoneCallbackInterface() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(TransactionListActivity.this, "Transação " +
                                                "Capturada com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError() {
                                        Toast.makeText(TransactionListActivity.this, "Ocorreu um " +
                                                "erro captura da transacao: " +
                                                provider.getListOfErrors(),
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });

                                provider.execute();
                                break;
                        }
                    }
                });
        builder.create();
        builder.show();
    }

    private void sendReceipt(TransactionObject selectedTransaction, ReceiptType receiptType) {
        SendEmailTransactionProvider sendEmailProvider = new SendEmailTransactionProvider(TransactionListActivity.this, selectedTransaction);
        sendEmailProvider.useDefaultUI(false);
        sendEmailProvider.setReceiptType(receiptType);
        sendEmailProvider.addTo(new Contact("cliente@gmail.com", "Nome do Cliente"));
        sendEmailProvider.setFrom(new Contact("loja@gmail.com", "Nome do Estabelecimento"));
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

    private void printReceipt(ReceiptType receiptType, TransactionObject transactionObject) {
        new PrintController(getApplicationContext(),
                new PosPrintReceiptProvider(getApplicationContext(),
                        transactionObject, receiptType)).print();
    }
}
