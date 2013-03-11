package com.KVP.ProcessMonitor;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class Server extends IntentService {
    final String TAG = "Server application" ;
    boolean work = true;
    String newApp = null;
    Calendar calendar = null;
    SimpleDateFormat pattern = null;
    public Server() {
        super("Server");
    }
    public void onCreate(){
        super.onCreate();
        pattern = new SimpleDateFormat();
        pattern.applyPattern("dd-MM-yyyy HH-mm-ss");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        List <ActivityManager.RunningAppProcessInfo> listNew = null;
        while (work){
            listNew = activityManager.getRunningAppProcesses();
                for (int j = 0; j < listNew.size(); j++)
                    if ((listNew.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)){
                        if (listNew.get(j).processName.equals("system") || listNew.get(j).processName.equals("com.android.phone") || listNew.get(j).processName.equals("com.android.systemui") || listNew.get(j).processName.equals(newApp));
                        else{
                            try {
                                calendar = Calendar.getInstance();
                                Log.v(TAG, pattern.format(calendar.getTime()) +" Запустили приложение: " + listNew.get(j).processName);
                                FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
                                fileWriter.append(String.valueOf(pattern.format(calendar.getTime())) + " Запустили приложение: "+ listNew.get(j).processName + "\n");
                                fileWriter.close();
                                fileWriter = null;
                                newApp = listNew.get(j).processName;
                                listNew = null;
                                break;
                            } catch (FileNotFoundException fnfe) {
                                Log.e(TAG, "Ошибка открытия файла: ", fnfe);
                            } catch (IOException ioe) {
                                Log.e(TAG, "Ошибка записи в файл: ", ioe);
                            } catch (Exception e){
                                Log.e(TAG, "Неизвестная ошибка при записи: ", e);
                            }
                        }
                    }
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                Log.e(TAG, "Неизвестная ошибка при задержке: ", e);
            }
        }
    }
    public void onDestroy(){
        work = false;
        super.onDestroy();
    }
}
