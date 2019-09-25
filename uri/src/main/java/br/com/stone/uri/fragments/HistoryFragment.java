package br.com.stone.uri.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.com.stone.uri.R;
import br.com.stone.uri.code.Response;
import br.com.stone.uri.db.Transaction;
import br.com.stone.uri.support.SharedPreferencesManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

import static android.content.Intent.ACTION_VIEW;
import static java.lang.String.format;

public class HistoryFragment extends Fragment {
    private static final String TAG = "ListTransactionsActivit";
    private static final int CANCELLATION_RESULT = 10;

    @BindView(R.id.transactionsListView)
    ListView transactionsListView;
    List<Transaction> transactions = new Select().from(Transaction.class).execute();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<String> transactionsAsString = new ArrayList<>();

        for (Transaction transaction : transactions) {
            transactionsAsString.add(format(new Locale("pt", "BR"), "ITK: %s\nATK: %s\nCode: %d\nReason: %s",
                    transaction.getPaymentId(),
                    transaction.getAcquirerAuthorizationCode(),
                    transaction.getResponseCode(),
                    transaction.getResponseReason()
            ));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, transactionsAsString);
        transactionsListView.setAdapter(adapter);
    }

    @OnItemClick(R.id.transactionsListView)
    public void onTransactionClicked(int position) {
        // create a new URI to request a cancel
        Uri.Builder transactionUri = new Uri.Builder();
        transactionUri.scheme("stone");
        transactionUri.authority("payment-reversal");
        transactionUri.appendQueryParameter("scheme", "demoUri");
        transactionUri.appendQueryParameter("paymentId", transactions.get(position).getPaymentId());
        transactionUri.appendQueryParameter("acquirerId", SharedPreferencesManager.newInstance(getContext()).getStoneCode());
        Intent intent = new Intent(ACTION_VIEW);
        intent.setData(transactionUri.build());
        startActivityForResult(intent, CANCELLATION_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            Response response = new Response(data.getData());
            Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data.getData().toString() + "]");
            Toast.makeText(getContext(), response.getReason(), Toast.LENGTH_SHORT).show();
            for (Transaction transaction : transactions) {
                if (transaction.getPaymentId().equals(response.getPaymentId())) {
                    if (response.getResponseCode() == 0) {
                        transaction.updateStatus(response.getResponseCode());
                    }
                }
            }
        }
    }

}
