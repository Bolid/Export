package com.KVP.ProcessMonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ComponentName app;
        final Intent startServer = new Intent(getBaseContext(), Server.class);
        Button butPlay = (Button)findViewById(R.id.button);
        Button butStop = (Button)findViewById(R.id.button1);
        butPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(startServer);
                Toast.makeText(getBaseContext(), "Служба запущена", Toast.LENGTH_LONG).show();
            }
        });
        butStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Server server = new Server();
                stopService(startServer);
                Toast.makeText(getBaseContext(), "Служба остановлена", Toast.LENGTH_LONG).show();
                server = null;
            }
        });
    }
}
