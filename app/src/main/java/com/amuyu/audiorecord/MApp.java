package com.amuyu.audiorecord;

import android.app.Application;

import com.amuyu.logger.DefaultLogPrinter;
import com.amuyu.logger.Logger;


public class MApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogPrinter(new DefaultLogPrinter(this));
    }
}
