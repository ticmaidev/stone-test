package br.com.stone.uri.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import br.com.stone.uri.R;
import br.com.stone.uri.code.Response;
import br.com.stone.uri.db.Transaction;
import br.com.stone.uri.support.SharedPreferencesManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;
import static android.content.Intent.ACTION_VIEW;
import static br.com.stone.uri.R.array.installment;
import static java.lang.String.valueOf;
import static java.util.UUID.randomUUID;

public class TransactionFragment extends Fragment {
    private static final String TAG = "TransactionFragment";

    @BindView(R.id.editTextValue)
    EditText editTextValue;
    @BindView(R.id.radioDebit)
    RadioButton debitRadioButton;
    @BindView(R.id.interestSwitch)
    Switch interestSwitch;
    @BindView(R.id.spinnerInstallments)
    Spinner installmentSpinner;

    int TRANSACTION_RESULT = 10;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureSpinner();
    }

    private void configureSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), installment, simple_spinner_item);
        adapter.setDropDownViewResource(simple_spinner_dropdown_item);
        installmentSpinner.setAdapter(adapter);
    }

    /**
     * App request example
     * <p>
     * {
     * acquirerProtocol: "stone",
     * action: "payment",
     * acquirerId: "954090369",
     * installments: 3,
     * paymentType: "credit",
     * amount: 10, // as apps esperam o valor em centavos (10 centavos)
     * installmentsInterestRate: "1%", (se não tiver juros, então não é nem para estar no mobileLinkingUrl)
     * transactionId: "1093019039",
     * paymentId: "1093019888",
     * scheme: "instore",
     * autoConfirm: "true"
     * }
     */

    @OnClick(R.id.buttonSendTransaction)
    public void sendTransaction() {
        Uri.Builder transactionUri = new Uri.Builder()
                .scheme("stone")
                .authority("payment")
                .appendQueryParameter("acquirerId", SharedPreferencesManager.newInstance(getContext()).getStoneCode())
                .appendQueryParameter("taxes", Boolean.toString(interestSwitch.isChecked()))
                .appendQueryParameter("transactionId", randomUUID().toString())
                .appendQueryParameter("paymentId", randomUUID().toString())
                .appendQueryParameter("paymentType", debitRadioButton.isChecked() ? "DEBIT" : "CREDIT")
                .appendQueryParameter("amount", editTextValue.getText().toString())
                .appendQueryParameter("scheme", "demoUri")
                .appendQueryParameter("installments", valueOf(installmentSpinner.getSelectedItemPosition() + 1))
                .appendQueryParameter("autoConfirm", "true"); // true = automatically | false = user needs to confirm

        Intent intent = new Intent(ACTION_VIEW);
        intent.setData(transactionUri.build());
        startActivityForResult(intent, TRANSACTION_RESULT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Log.d(TAG, data.toUri(0));
            Response response = new Response(data.getData());
            Toast.makeText(getContext(), response.getReason(), Toast.LENGTH_SHORT).show();
            new Transaction(response).save();
        }
    }
}
