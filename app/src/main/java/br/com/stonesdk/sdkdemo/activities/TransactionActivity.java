package br.com.stonesdk.sdkdemo.activities;

import android.widget.Toast;

import stone.application.enums.ErrorsEnum;
import stone.application.enums.TransactionStatusEnum;
import stone.application.interfaces.StoneCallbackInterface;
import stone.providers.LoadTablesProvider;
import stone.providers.TransactionProvider;
import stone.utils.Stone;

public class TransactionActivity extends BaseTransactionActivity<TransactionProvider> {

    @Override
    protected TransactionProvider buildTransactionProvider() {
        return new TransactionProvider(TransactionActivity.this, transactionObject, getSelectedUserModel(), Stone.getPinpadFromListAt(0));
    }

    @Override
    public void onSuccess() {
        if (transactionObject.getTransactionStatus() == TransactionStatusEnum.APPROVED) {
            Toast.makeText(getApplicationContext(), "Transação enviada com sucesso e salva no banco. Para acessar, use o TransactionDAO.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Erro na transação: \"" + getAuthorizationMessage() + "\"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onError()   {
        super.onError();
        if (providerHasErrorEnum(ErrorsEnum.NEED_LOAD_TABLES)) { // code 20
            LoadTablesProvider loadTablesProvider = new LoadTablesProvider(TransactionActivity.this, Stone.getPinpadFromListAt(0));
            loadTablesProvider.setDialogMessage("Subindo as tabelas");
            loadTablesProvider.useDefaultUI(true); // para dar feedback ao usuario ou nao.
            loadTablesProvider.execute();
            loadTablesProvider.setConnectionCallback(new StoneCallbackInterface() {
                public void onSuccess() {
                    initTransaction(); // reinicia a transação
                }

                public void onError() {
                    Toast.makeText(getApplicationContext(), "Sucesso.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
