package com.example.lenovo.myalbumtest;

import android.app.Application;
import android.content.Context;


/**
 * Created by lenovo on 2017/8/22.
 */

public class MyApplication extends Application {

    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }


    public static MyApplication getInstance() {
        return myApplication;
    }


}
