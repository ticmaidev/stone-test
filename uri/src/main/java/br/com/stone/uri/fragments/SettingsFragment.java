package br.com.stone.uri.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import br.com.stone.uri.R;
import br.com.stone.uri.code.Response;
import br.com.stone.uri.support.SharedPreferencesManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.ACTION_VIEW;
import static android.widget.Toast.LENGTH_SHORT;

public class SettingsFragment extends Fragment {
    private static final int CONFIG_RESULT = 1;
    private static final String TAG = "SettingsFragment";

    @BindView(R.id.stoneCodeEditText)
    TextInputEditText stoneCodeEditText;
    SharedPreferencesManager sharedPreferencesManager;
    String stoneCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferencesManager = SharedPreferencesManager.newInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String stoneCode = sharedPreferencesManager.getStoneCode();
        stoneCodeEditText.setText(stoneCode);
    }

    @OnClick(R.id.saveButton)
    public void save() {

        stoneCode = stoneCodeEditText.getText().toString();

        Uri.Builder transactionUri = new Uri.Builder();
        transactionUri.scheme("stone");
        transactionUri.authority("configuration");
        transactionUri.appendQueryParameter("acquirerId", stoneCode);
        transactionUri.appendQueryParameter("scheme", "demoUri");
        Intent intent = new Intent(ACTION_VIEW);
        intent.setData(transactionUri.build());
        startActivityForResult(intent, CONFIG_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "]");
        if (data != null && data.getData() != null) {
            Log.d(TAG, "data = [" + data.getData().toString() + "]");
            Response response = new Response(data.getData());
            Toast.makeText(getContext(), response.getReason(), LENGTH_SHORT).show();
            if (response.getResponseCode() == 0) {
                sharedPreferencesManager.setStoneCode(stoneCode);
            }
        } else {
            Toast.makeText(getContext(), "no data content", LENGTH_SHORT).show();
        }
    }
}