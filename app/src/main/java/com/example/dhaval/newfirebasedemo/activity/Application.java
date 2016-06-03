package com.example.dhaval.newfirebasedemo.activity;

import android.content.Intent;

import com.example.dhaval.newfirebasedemo.FireBaseNodeListner;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by dhaval on 1/6/16.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);


        startService(new Intent(Application.this, FireBaseNodeListner.class));
    }
}
