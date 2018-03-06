package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import br.com.stone.posandroid.providers.PosPrintProvider;
import br.com.stone.posandroid.providers.PosTransactionProvider;
import stone.application.enums.ErrorsEnum;
import stone.application.enums.TransactionStatusEnum;
import stone.application.interfaces.StoneCallbackInterface;
import stone.utils.Stone;

public class PosAndroidActivity extends BaseTransactionActivity<PosTransactionProvider> {

    @Override
    protected PosTransactionProvider buildTransactionProvider() {
        return new PosTransactionProvider(this, transactionObject, Stone.getUserModel(0));
    }

    @Override
    public void onSuccess() {
        if (transactionObject.getTransactionStatus() == TransactionStatusEnum.APPROVED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final PosPrintProvider posPrintProvider = new PosPrintProvider(PosAndroidActivity.this, transactionObject);
                    posPrintProvider.setConnectionCallback(new StoneCallbackInterface() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(PosAndroidActivity.this, "Recibo impresso", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError() {
                            Toast.makeText(PosAndroidActivity.this, "Erro ao imprimir: " + posPrintProvider.getListOfErrors(), Toast.LENGTH_SHORT).show();
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
