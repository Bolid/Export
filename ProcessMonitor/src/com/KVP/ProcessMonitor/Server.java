package com.KVP.ProcessMonitor;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
        PackageManager packageManager = this.getPackageManager();
        ApplicationInfo ai;
        List <ActivityManager.RunningAppProcessInfo> listNew = null;
        List<ApplicationInfo> allApplication = packageManager.getInstalledApplications(0);
        try {
            FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
            fileWriter.append( "Установленные приложения:\n");
            for (int j = 0; j < allApplication.size(); j++){
                fileWriter.append(j+1+") Пакет: "+allApplication.get(j).packageName+" Название приложения: "+packageManager.getApplicationLabel(allApplication.get(j)).toString()+"\n");
            }
            fileWriter.close();
            fileWriter = null;
        }
        catch (FileNotFoundException fnfe){
            Log.e(TAG, "Ошибка открытия файла: ", fnfe);
        } catch (IOException ioe) {
            Log.e(TAG, "Ошибка записи в файл: ", ioe);
        }
        Long timeStart = null;
        Long timeStop;
        while (work){
            listNew = activityManager.getRunningAppProcesses();
            if (newApp != null)
                for (int j = 0; j < listNew.size(); j++){
                    if (listNew.get(j).processName.equals(newApp))
                        if (listNew.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
                            calendar = Calendar.getInstance();
                            timeStop = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                            try {
                                FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
                                fileWriter.append(String.valueOf(pattern.format(calendar.getTime())) + " Закрыли процесс: "+ newApp + "\n");
                                fileWriter.append("\t\t\t\t\t\t\t\t\t\tПроцесс "+newApp+" работал: "+ getTimeWork(timeStart, timeStop)+"\n");
                                fileWriter.append("--------------------------------------------\n");
                                fileWriter.close();
                                fileWriter = null;
                                calendar = Calendar.getInstance();
                                Log.v(TAG, pattern.format(calendar.getTime()) +" Закрыли процесс: " + newApp);
                            } catch (FileNotFoundException fnfe){
                                Log.e(TAG, "Ошибка открытия файла: ", fnfe);
                            } catch (IOException ioe) {
                                Log.e(TAG, "Ошибка записи в файл: ", ioe);
                            } catch (Exception e){
                                Log.e(TAG, "Неизвестная ошибка при записи: ", e);
                            }
                            newApp = null;
                            break;
                        }
                }
                for (int j = 0; j < listNew.size(); j++){
                    if (listNew.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                        if (
                                listNew.get(j).processName.equals("system") || listNew.get(j).processName.equals("com.android.phone") ||
                                listNew.get(j).processName.equals("com.android.systemui") || listNew.get(j).processName.equals("com.android.launcher") ||
                                listNew.get(j).processName.equals("android.process.acore") || listNew.get(j).processName.equals("com.android.nfc") ||
                                listNew.get(j).processName.equals(newApp)){
                        }
                        else{
                            try {
                                ai = (ApplicationInfo)packageManager.getApplicationInfo(listNew.get(j).processName, 0);
                                newApp = listNew.get(j).processName;
                                calendar = Calendar.getInstance();
                                timeStart = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                                Log.v(TAG, pattern.format(calendar.getTime()) +" Запустили приложение: " + newApp +" Название: "+ packageManager.getApplicationLabel(ai));
                                FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
                                fileWriter.append("--------------------------------------------\n");
                                fileWriter.append(String.valueOf(pattern.format(calendar.getTime())) + " Запустили процесс: "+ newApp +" Название приложения: "+packageManager.getApplicationLabel(ai)+"\n");
                                fileWriter.close();
                                fileWriter = null;
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
                }
            try {
                listNew = null;
                System.gc();
                //Thread.sleep(5000);
            } catch (Exception e) {
                Log.e(TAG, "Неизвестная ошибка при задержке: ", e);
            }
        }
    }

    public void onDestroy(){
        work = false;
        super.onDestroy();
    }

    public String getTimeWork(Long timeStart, Long timeStop){
        Long timeWork = timeStop - timeStart;
        int hour = 0;
        int minute = 0;
        int second = 0;

        while (timeWork >= 1000){
            if (timeWork >= 3600000){
                timeStart -= 3600000;
                hour++;
            }
            else if (timeWork >= 60000){
                timeWork -= 60000;
                minute++;
            }
            else if (timeWork >= 1000){
                timeWork -= 1000;
                second++;
            }
        }
        return String.valueOf(hour) +":"+ String.valueOf(minute) +":"+ String.valueOf(second);
    }
}
