package br.com.stonesdk.sdkdemo.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;

import br.com.stone.posandroid.providers.PosTransactionProvider;
import br.com.stonesdk.sdkdemo.controller.PrintController;
import stone.application.enums.Action;
import stone.application.enums.ErrorsEnum;
import stone.application.enums.ReceiptType;
import stone.application.enums.TransactionStatusEnum;

public class PosTransactionActivity extends BaseTransactionActivity<PosTransactionProvider> {

    @Override
    protected PosTransactionProvider buildTransactionProvider() {
        return new PosTransactionProvider(this, transactionObject, getSelectedUserModel());
    }

    @Override
    public void onSuccess() {
        if (transactionObject.getTransactionStatus() == TransactionStatusEnum.APPROVED) {

            final PrintController printController = new PrintController(
                    PosTransactionActivity.this,
                    transactionObject
            );

            printController.print(ReceiptType.MERCHANT);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Transação aprovada! Deseja imprimir a via do cliente?");

            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    printController.print(ReceiptType.CLIENT);
                }
            });

            builder.setNegativeButton(android.R.string.no, null);
            builder.show();

        } else {
            Toast.makeText(
                    getApplicationContext(),
                    "Erro na transação: \"" + getAuthorizationMessage() + "\"",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    @Override
    public void onError() {
        super.onError();
        if (providerHasErrorEnum(ErrorsEnum.DEVICE_NOT_COMPATIBLE)) {
            Toast.makeText(
                    this,
                    "Dispositivo não compatível ou dependência relacionada não está presente",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onStatusChanged(final Action action) {
        super.onStatusChanged(action);
        switch (action) {
            case TRANSACTION_WAITING_PASSWORD:
                Toast.makeText(
                        this,
                        "Pin tries remaining to block card: ${transactionProvider?.remainingPinTries}",
                        Toast.LENGTH_LONG
                ).show();
        }
    }

}
