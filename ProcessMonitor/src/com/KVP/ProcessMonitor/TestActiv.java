package com.KVP.ProcessMonitor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;


public class TestActiv extends Activity{

    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.main);
        PackageManager packageManager = getPackageManager();
        //packageManager.getLaunchIntentForPackage("com.android.settings");
        Intent intent1 = packageManager.getLaunchIntentForPackage("getLaunchIntentForPackage");
        //intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent1);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_LAUNCHER);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        Log.v("Server application", "Проба: " + intentFilter);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v("Server application", "Проба: ");
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }
}
