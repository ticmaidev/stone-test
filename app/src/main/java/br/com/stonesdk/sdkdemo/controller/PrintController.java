package br.com.stonesdk.sdkdemo.controller;

import android.content.Context;
import android.widget.Toast;

import br.com.stone.posandroid.providers.PosPrintReceiptProvider;
import stone.application.enums.ReceiptType;
import stone.application.interfaces.StoneCallbackInterface;
import stone.database.transaction.TransactionObject;

/**
 * @author tiago.barbosa
 * @since 29/10/2018
 */
public class PrintController {

    private Context context;
    private TransactionObject transactionObject;

    public PrintController(Context context, TransactionObject transactionObject) {
        this.context = context;
        this.transactionObject = transactionObject;
    }

    public void print(final ReceiptType receiptType) {
        final PosPrintReceiptProvider posPrintReceiptProviderInstance =
                getPosPrintReceiptProviderInstance();

        posPrintReceiptProviderInstance.setConnectionCallback(new StoneCallbackInterface() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Recibo impresso", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError() {
                Toast.makeText(context, "Erro ao imprimir: " + posPrintReceiptProviderInstance
                        .getListOfErrors(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private PosPrintReceiptProvider getPosPrintReceiptProviderInstance() {
        return new PosPrintReceiptProvider(
                context,
                transactionObject,
                ReceiptType.MERCHANT
        );
    }

}
