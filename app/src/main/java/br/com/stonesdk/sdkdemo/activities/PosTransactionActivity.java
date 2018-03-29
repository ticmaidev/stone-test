package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.stone.posandroid.providers.PosTransactionProvider;
import stone.application.enums.ErrorsEnum;
import stone.application.enums.TransactionStatusEnum;
import stone.application.interfaces.StoneCallbackInterface;

public class PosTransactionActivity extends BaseTransactionActivity<PosTransactionProvider> {

    @Override
    protected PosTransactionProvider buildTransactionProvider() {
        return new PosTransactionProvider(this, transactionObject, getSelectedUserModel());
    }

    @Override
    public void onSuccess() {
        if (transactionObject.getTransactionStatus() == TransactionStatusEnum.APPROVED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Transação aprovada! Deseja imprimir o comprovante?");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final PosPrintProvider posPrintProvider = new PosPrintProvider(PosTransactionActivity.this, transactionObject);
                    posPrintProvider.setConnectionCallback(new StoneCallbackInterface() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(PosTransactionActivity.this, "Recibo impresso", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(PosTransactionActivity.this, "Erro ao imprimir: " + posPrintProvider.getListOfErrors(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
        } else {
            Toast.makeText(getApplicationContext(), "Erro na transação: \"" + getAuthorizationMessage() + "\"", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onError() {
        super.onError();
        if (providerHasErrorEnum(ErrorsEnum.DEVICE_NOT_COMPATIBLE)) {
            Toast.makeText(this, "Dispositivo não compatível ou dependência relacionada não está presente", Toast.LENGTH_SHORT).show();
        }
    }
}
