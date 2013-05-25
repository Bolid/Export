package com.KVP.ProcessMonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class ApplicationStartListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "FUI", Toast.LENGTH_LONG).show();
            Log.v("Server application FUI", intent.getAction());
    }
}
