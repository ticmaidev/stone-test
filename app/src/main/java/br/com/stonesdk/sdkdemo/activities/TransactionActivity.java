package br.com.stonesdk.sdkdemo.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.com.stonesdk.sdkdemo.R;
import stone.application.enums.ErrorsEnum;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.LoadTablesProvider;
import stone.providers.TransactionProvider;
import stone.utils.Stone;
import stone.utils.StoneTransaction;

public class TransactionActivity extends AppCompatActivity {

    TextView valueTextView;
    TextView numberInstallmentsTextView;
    EditText valueEditText;
    RadioGroup radioGroup;
    RadioButton debitRadioButton;
    Button sendButton;
    Spinner instalmentsSpinner;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        valueTextView = (TextView) findViewById(R.id.textViewValue);
        numberInstallmentsTextView = (TextView) findViewById(R.id.textViewInstallments);
        valueEditText = (EditText) findViewById(R.id.editTextValue);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupDebitCredit);
        sendButton = (Button) findViewById(R.id.buttonSendTransaction);
        instalmentsSpinner = (Spinner) findViewById(R.id.spinnerInstallments);
        debitRadioButton = (RadioButton) findViewById(R.id.radioDebit);

        numberInstallmentsTextView.setVisibility(View.INVISIBLE);
        instalmentsSpinner.setVisibility(View.INVISIBLE);

        spinnerAction();
        radioGroupClick();
        sendTransaction();
    }

    private void radioGroupClick() {
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioDebit) {
                    numberInstallmentsTextView.setVisibility(View.INVISIBLE);
                    instalmentsSpinner.setVisibility(View.INVISIBLE);
                } else {
                    numberInstallmentsTextView.setVisibility(View.VISIBLE);
                    instalmentsSpinner.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void sendTransaction() {

        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // Cria o objeto de transacao. Usar o "Stone.getPinpadFromListAt"
                // significa que devera estar conectado com ao menos um pinpad, pois o metodo
                // cria uma lista de conectados e conecta com quem estiver na posicao "0".
                StoneTransaction stoneTransaction = new StoneTransaction(Stone.getPinpadFromListAt(0));

                // A seguir deve-se popular o objeto.
                stoneTransaction.setAmount(valueEditText.getText().toString());
                stoneTransaction.setEmailClient(null);
                stoneTransaction.setUserModel(Stone.getUserModel(0));

                // AVISO IMPORTANTE: Nao e recomendado alterar o campo abaixo do
                // ITK, pois ele gera um valor unico. Contudo, caso seja
                // necessario, faca conforme a linha abaixo.
                stoneTransaction.setInitiatorTransactionKey("SEU_IDENTIFICADOR_UNICO_AQUI");

                // Informa a quantidade de parcelas.
                stoneTransaction.setInstalmentTransactionEnum(InstalmentTransactionEnum.getAt(instalmentsSpinner.getSelectedItemPosition()));

                // Verifica a forma de pagamento selecionada.
                if (debitRadioButton.isChecked()) {
                    stoneTransaction.setTypeOfTransaction(TypeOfTransactionEnum.DEBIT);
                } else {
                    stoneTransaction.setTypeOfTransaction(TypeOfTransactionEnum.CREDIT);
                }

                // Processo para envio da transacao.
                final TransactionProvider provider = new TransactionProvider(TransactionActivity.this, stoneTransaction, Stone.getPinpadFromListAt(0));
                provider.setWorkInBackground(false);
                provider.setDialogMessage("Enviando..");
                provider.setDialogTitle("Aguarde");

                provider.setConnectionCallback(new StoneCallbackInterface() {
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    public void onError() {
                        Toast.makeText(getApplicationContext(), "Erro na transação", Toast.LENGTH_SHORT).show();
                        if (provider.theListHasError(ErrorsEnum.NEED_LOAD_TABLES)) { // code 20
                            LoadTablesProvider loadTablesProvider = new LoadTablesProvider(TransactionActivity.this, provider.getGcrRequestCommand(), Stone.getPinpadFromListAt(0));
                            loadTablesProvider.setDialogMessage("Subindo as tabelas");
                            loadTablesProvider.setWorkInBackground(false); // para dar feedback ao usuario ou nao.
                            loadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                                public void onSuccess() {
                                    sendButton.performClick(); // simula um clique no botao de enviar transacao para reenviar a transacao.
                                }

                                public void onError() {
                                    Toast.makeText(getApplicationContext(), "Sucesso.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            loadTablesProvider.execute();
                        }
                    }
                });
                provider.execute();
            }
        });
    }

    private void spinnerAction() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.installments_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instalmentsSpinner.setAdapter(adapter);
    }

}
