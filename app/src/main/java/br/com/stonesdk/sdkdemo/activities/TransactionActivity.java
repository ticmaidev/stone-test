package br.com.stonesdk.sdkdemo.activities;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.com.stonesdk.sdkdemo.R;
import stone.application.enums.Action;
import stone.application.enums.InstalmentTransactionEnum;
import stone.application.enums.TransactionStatusEnum;
import stone.application.enums.TypeOfTransactionEnum;
import stone.application.interfaces.StoneActionCallback;
import stone.database.transaction.TransactionObject;
import stone.providers.TransactionProvider;
import stone.utils.Stone;

public class TransactionActivity extends AppCompatActivity {

    TextView numberInstallmentsTextView;
    EditText valueEditText;
    RadioGroup radioGroup;
    RadioButton debitRadioButton;
    Button sendButton;
    Spinner instalmentsSpinner;
    CheckBox captureTransactionCheckBox;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        numberInstallmentsTextView = findViewById(R.id.textViewInstallments);
        valueEditText = findViewById(R.id.editTextValue);
        radioGroup = findViewById(R.id.radioGroupDebitCredit);
        sendButton = findViewById(R.id.buttonSendTransaction);
        instalmentsSpinner = findViewById(R.id.spinnerInstallments);
        debitRadioButton = findViewById(R.id.radioDebit);
        captureTransactionCheckBox = findViewById(R.id.captureTransactionCheckBox);

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

                // Cria o objeto de transacao.
                TransactionObject transactionObject = new TransactionObject();

                // A seguir deve-se popular o objeto.
                transactionObject.setAmount(valueEditText.getText().toString());
                transactionObject.setUserModel(Stone.getUserModel(0));
                transactionObject.setSignature(BitmapFactory.decodeResource(getResources(), R.drawable.signature));
                transactionObject.setCapture(captureTransactionCheckBox.isChecked());

                transactionObject.setSubMerchantCity("city"); //Cidade do sub-merchant
                transactionObject.setSubMerchantPostalAddress("00000000"); //CEP do sub-merchant (Apenas números)
                transactionObject.setSubMerchantRegisteredIdentifier("00000000"); // Identificador do sub-merchant
                transactionObject.setSubMerchantTaxIdentificationNumber("33368443000199"); // CNPJ do sub-merchant (apenas números)

                //        Seleciona o mcc do lojista.
                transactionObject.setSubMerchantCategoryCode("123");

                //        Seleciona o endereço do lojista.
                transactionObject.setSubMerchantAddress("address");

                // AVISO IMPORTANTE: Nao e recomendado alterar o campo abaixo do
                // ITK, pois ele gera um valor unico. Contudo, caso seja
                // necessario, faca conforme a linha abaixo.
//                transactionObject.setInitiatorTransactionKey("SEU_IDENTIFICADOR_UNICO_AQUI");

                // Informa a quantidade de parcelas.
                transactionObject.setInstalmentTransaction(InstalmentTransactionEnum.getAt(instalmentsSpinner.getSelectedItemPosition()));

                // Verifica a forma de pagamento selecionada.
                if (debitRadioButton.isChecked()) {
                    transactionObject.setTypeOfTransaction(TypeOfTransactionEnum.DEBIT);
                } else {
                    transactionObject.setTypeOfTransaction(TypeOfTransactionEnum.CREDIT);
                }

                // Processo para envio da transacao.
                final TransactionProvider provider = new TransactionProvider(
                        TransactionActivity.this,
                        transactionObject,
                        Stone.getUserModel(0),
                        Stone.getPinpadFromListAt(0)
                );

                provider.useDefaultUI(false);
                provider.setDialogMessage("Enviando..");
                provider.setDialogTitle("Aguarde");

                provider.setConnectionCallback(new StoneActionCallback() {
                    @Override
                    public void onStatusChanged(Action action) {
                        Log.d("TRANSACTION_STATUS", action.name());
                    }

                    public void onSuccess() {
                        if (provider.getTransactionStatus() == TransactionStatusEnum.APPROVED) {
                            Toast.makeText(getApplicationContext(), "Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Erro na transação: \"" + provider.getMessageFromAuthorize() + "\"", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }

                    public void onError() {
                        Toast.makeText(getApplicationContext(), "Erro na transação", Toast.LENGTH_SHORT).show();
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
