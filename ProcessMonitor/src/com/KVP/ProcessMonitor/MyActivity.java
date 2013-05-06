package com.KVP.ProcessMonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
        Button butPost = (Button)findViewById(R.id.button2);
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
        butPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task task = new Task();
                task.execute();
            }
        });
    }
    class Task extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... objects) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://wia-games.net/projects/appfield/api/api.php");
                List<NameValuePair> parameters = new ArrayList<NameValuePair>(2);
                JSONObject jsonObject = new JSONObject();
                String json = "{\"dev_id\":\"1234\",\"model\":\"1234\",\"battery_level\":12,\"free_memory\":\"120\",\"os_locale\":\"ru-ru\",\"keyboard_locale\":\"ru-ru\",\"os_version\":17}";
                //{"keyboard_locale":"ru-ru","battery_level":12,"model":"1234","os_locale":"ru-ru","free_memory":"120","dev_id":"1234","os_version":17}
                //{"battery_level":12,"keyboard_locale":"ru-ru","model":"1234","free_memory":"120","os_locale":"ru-ru","dev_id":"1234","os_version":17}
                jsonObject.put("keyboard_locale", "ru-ru");
                jsonObject.put("battery_level", 12);
                jsonObject.put("model", "1234");
                jsonObject.put("os_locale", "en-us");
                jsonObject.put("free_memory", 120);
                jsonObject.put("dev_id", "1234767hjgjh");
                jsonObject.put("os_version", 17);
                parameters.add(new BasicNameValuePair("action", "update_user_info"));
                parameters.add(new BasicNameValuePair("data", jsonObject.toString()));
                httpPost.setEntity(new UrlEncodedFormEntity(parameters));

                //httpPost.setHeader("Content-Type", "application/json");
                httpPost.setHeader("Accept-Encoding", "application/json");
                httpPost.setHeader("Accept-Language", "en-US");
                HttpResponse httpResponse = httpClient.execute(httpPost);
                Log.v("Server Application", EntityUtils.toString(httpResponse.getEntity())+" "+jsonObject.toString());

            } catch (UnsupportedEncodingException e) {
                Log.e("Server Application", "Error: " + e);
            } catch (ClientProtocolException e) {
                Log.e("Server Application", "Error: " + e);
            } catch (IOException e) {
                Log.e("Server Application", "Error: " + e);
            } catch (JSONException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            return null;
        }
    }
}
