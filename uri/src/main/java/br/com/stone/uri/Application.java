package br.com.stone.uri;

import com.activeandroid.ActiveAndroid;

/**
 * @author frodrigues
 * @since 04/05/2018
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        ActiveAndroid.dispose();
    }
}
