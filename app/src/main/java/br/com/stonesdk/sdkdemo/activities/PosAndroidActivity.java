package br.com.stonesdk.sdkdemo.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PosAndroidActivity extends BaseTransactionActivity<Traprov> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos_android);
    }
}
