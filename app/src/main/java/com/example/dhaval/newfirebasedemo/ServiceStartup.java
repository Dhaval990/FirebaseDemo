package com.example.dhaval.newfirebasedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStartup extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
       context.startService(new Intent(context,FireBaseNodeListner.class));
    }
}