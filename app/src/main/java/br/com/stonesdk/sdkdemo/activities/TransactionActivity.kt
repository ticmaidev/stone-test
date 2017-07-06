package br.com.stonesdk.sdkdemo.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import br.com.stonesdk.sdkdemo.R
import stone.application.enums.ErrorsEnum
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.LoadTablesProvider
import stone.providers.TransactionProvider
import stone.utils.GlobalInformations
import stone.utils.StoneTransaction

class TransactionActivity : AppCompatActivity() {

    internal var valueTextView: TextView
    internal var numberInstallmentsTextView: TextView
    internal var valueEditText: EditText
    internal var radioGroup: RadioGroup
    internal var debitRadioButton: RadioButton
    internal var sendButton: Button
    internal var instalmentsSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        valueTextView = findViewById(R.id.textViewValue) as TextView
        numberInstallmentsTextView = findViewById(R.id.textViewInstallments) as TextView
        valueEditText = findViewById(R.id.editTextValue) as EditText
        radioGroup = findViewById(R.id.radioGroupDebitCredit) as RadioGroup
        sendButton = findViewById(R.id.buttonSendTransaction) as Button
        instalmentsSpinner = findViewById(R.id.spinnerInstallments) as Spinner
        debitRadioButton = findViewById(R.id.radioDebit) as RadioButton

        numberInstallmentsTextView.visibility = View.INVISIBLE
        instalmentsSpinner.visibility = View.INVISIBLE

        spinnerAction()
        radioGroupClick()
        sendTransaction()
    }

    private fun radioGroupClick() {
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.radioDebit) {
                numberInstallmentsTextView.visibility = View.INVISIBLE
                instalmentsSpinner.visibility = View.INVISIBLE
            } else {
                numberInstallmentsTextView.visibility = View.VISIBLE
                instalmentsSpinner.visibility = View.VISIBLE
            }
        }
    }

    fun sendTransaction() {

        sendButton.setOnClickListener {
            // Cria o objeto de transacao. Usar o "GlobalInformations.getPinpadFromListAt"
            // significa que devera estar conectado com ao menos um pinpad, pois o metodo
            // cria uma lista de conectados e conecta com quem estiver na posicao "0".
            val stoneTransaction = StoneTransaction(GlobalInformations.getPinpadFromListAt(0))

            // A seguir deve-se popular o objeto.
            stoneTransaction.amount = valueEditText.text.toString()
            stoneTransaction.emailClient = null
            stoneTransaction.requestId = null
            stoneTransaction.userModel = GlobalInformations.getUserModel(0)

            // AVISO IMPORTANTE: Nao e recomendado alterar o campo abaixo do
            // ITK, pois ele gera um valor unico. Contudo, caso seja
            // necessario, faca conforme a linha abaixo.
            stoneTransaction.initiatorTransactionKey = "SEU_IDENTIFICADOR_UNICO_AQUI"

            // Informa a quantidade de parcelas.
            stoneTransaction.instalmentTransactionEnum = InstalmentTransactionEnum.getAt(instalmentsSpinner.selectedItemPosition)

            // Verifica a forma de pagamento selecionada.
            if (debitRadioButton.isChecked) {
                stoneTransaction.typeOfTransaction = TypeOfTransactionEnum.DEBIT
            } else {
                stoneTransaction.typeOfTransaction = TypeOfTransactionEnum.CREDIT
            }

            // Processo para envio da transacao.
            val provider = TransactionProvider(this@TransactionActivity, stoneTransaction, GlobalInformations.getPinpadFromListAt(0))
            provider.isWorkInBackground = false
            provider.dialogMessage = "Enviando.."
            provider.dialogTitle = "Aguarde"

            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    Toast.makeText(applicationContext, "Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO.", Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onError() {
                    Toast.makeText(applicationContext, "Erro na transação", Toast.LENGTH_SHORT).show()
                    if (provider.theListHasError(ErrorsEnum.NEED_LOAD_TABLES)) { // code 20
                        val loadTablesProvider = LoadTablesProvider(this@TransactionActivity, provider.gcrRequestCommand, GlobalInformations.getPinpadFromListAt(0))
                        loadTablesProvider.dialogMessage = "Subindo as tabelas"
                        loadTablesProvider.isWorkInBackground = false // para dar feedback ao usuario ou nao.
                        loadTablesProvider.connectionCallback = object : StoneCallbackInterface {
                            override fun onSuccess() {
                                sendButton.performClick() // simula um clique no botao de enviar transacao para reenviar a transacao.
                            }

                            override fun onError() {
                                Toast.makeText(applicationContext, "Sucesso.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        loadTablesProvider.execute()
                    }
                }
            }
            provider.execute()
        }
    }

    private fun spinnerAction() {
        val adapter = ArrayAdapter.createFromResource(this, R.array.installments_array, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        instalmentsSpinner.adapter = adapter
    }

}
