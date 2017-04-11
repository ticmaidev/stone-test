package br.com.stonesdk.sdkdemo.application;

import android.app.Application;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

/**
 * Created by JGabrielFreitas on 17/02/17.
 */
public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
}
