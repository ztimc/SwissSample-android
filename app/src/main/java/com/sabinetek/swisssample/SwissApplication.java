package com.sabinetek.swisssample;

import android.app.Application;

import com.sabinetek.swiss.Swiss;

public class SwissApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Swiss.initialize(this);
    }
}
