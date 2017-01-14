package com.example.dell.sunshine.app.sync;

/**
 * Created by DELL on 18/12/2016.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SunshineAuthenticatorService extends Service {
    private SunshineAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new SunshineAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}