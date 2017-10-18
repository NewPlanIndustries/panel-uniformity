package com.newplanindustries.paneluniformitytester;

import android.app.Application;

import timber.log.Timber;

public class PanelUniformityTesterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
