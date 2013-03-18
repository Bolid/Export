package com.KVP.ProcessMonitor;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class Server extends IntentService {
    final String TAG = "Server application" ;
    boolean work = true;
    Calendar calendar = null;
    SimpleDateFormat pattern = null;
    String app = "com.KVP.ProcessMonitor";
    public Server() {
        super("Server");
    }

    public void onCreate(){
        super.onCreate();
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        pattern = new SimpleDateFormat();
        pattern.applyPattern("dd-MM-yyyy HH-mm-ss");
        startForeground(1, new Notification());
    }




    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        PackageManager packageManager = this.getPackageManager();
        ApplicationInfo ai = null;
        List <ActivityManager.RunningAppProcessInfo> listApplicationAll = null;
        List <ActivityManager.RunningAppProcessInfo> listApplicationNew = new ArrayList<ActivityManager.RunningAppProcessInfo>();
        List <Long> timeApplicationStart = new ArrayList <Long>();
        Long timeStop;
        systemDevice(packageManager);
        while (work){
            listApplicationAll = activityManager.getRunningAppProcesses();
            if (listApplicationNew.isEmpty() == false)
            {
                for (int i = 0; i < listApplicationNew.size(); i++){
                    String nameApplication = listApplicationNew.get(i).processName;
                    Log.v(TAG, "Статус: " + listApplicationNew.get(i).importance);
                    for (int j = 0; j < listApplicationAll.size(); j++){
                        if (listApplicationAll.get(j).processName.equals(nameApplication) && listApplicationAll.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
                            calendar = Calendar.getInstance();
                            timeStop = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                            String timeWork = getTimeWork(timeApplicationStart.get(i), timeStop);
                            Log.v(TAG, pattern.format(calendar.getTime())+" Закрыт процесс: " + listApplicationNew.get(i).processName);
                            Log.v(TAG, "\t\t\t\t\t\t\t\tПроцесс: " + listApplicationNew.get(i).processName+" работал: "+timeWork);
                            //Log.v(TAG, "Статус приложения: " + listApplicationAll.get(j).importance);
                            writeLog(null, pattern.format(calendar.getTime()).toString(), timeWork, listApplicationNew.get(i).processName, null);
                            listApplicationNew.remove(i);
                            timeApplicationStart.remove(i);
                            if (listApplicationNew.isEmpty())
                                break;
                        }else if (listApplicationAll.get(j).processName.equals(nameApplication) && listApplicationAll.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            listApplicationAll.remove(j);
                        }
                    }
                }
            }
            for (int j = 0; j < listApplicationAll.size(); j++){
                if (listApplicationAll.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    if (listApplicationAll.get(j).processName.equals("system") || listApplicationAll.get(j).processName.equals("com.android.phone") ||
                            listApplicationAll.get(j).processName.equals("com.android.systemui") || listApplicationAll.get(j).processName.equals("com.android.launcher") ||
                            listApplicationAll.get(j).processName.equals("android.process.acore") || listApplicationAll.get(j).processName.equals("com.android.nfc") ||
                            listApplicationAll.get(j).processName.equals(getPackageName())){
                    }
                    else {
                        String nameAppication = null;
                        try {
                            ai = (ApplicationInfo)getPackageManager().getApplicationInfo(listApplicationAll.get(j).processName, 0);
                            nameAppication = packageManager.getApplicationLabel(ai).toString();
                        }
                        catch (PackageManager.NameNotFoundException pmnnfe) {
                            Log.e(TAG, "Ошибка при получении информации о пакете: "+ pmnnfe);
                        }
                            Long timeStart = null;
                            calendar = Calendar.getInstance();
                            timeStart = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                            timeApplicationStart.add(0, timeStart);
                            listApplicationNew.add(0, listApplicationAll.get(j));
                            Log.v(TAG, pattern.format(calendar.getTime()) + " Записали процесс: " + listApplicationAll.get(j).processName + " Название приложения: " + nameAppication);
                            writeLog(pattern.format(calendar.getTime()).toString(), null, null, listApplicationAll.get(j).processName, nameAppication);

                    }
                }
            }

           
            try {
                ai = null;
                listApplicationAll.clear();
                listApplicationAll = null;
                System.gc();
                Thread.sleep(1000);
            } catch (Exception e) {
                Log.e(TAG, "Неизвестная ошибка при задержке: ", e);
            }
        }
    }

    public void onDestroy(){
        work = false;
        stopForeground(false);
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

    public void systemDevice(PackageManager packageManager){
        List <ApplicationInfo> allApplication = packageManager.getInstalledApplications(0);
        Locale locale = Locale.getDefault();
        Log.v(TAG, "Локаль: "+ locale.getCountry()+" "+locale.getDisplayCountry());
        Build build = new Build();
        try {
            FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
            fileWriter.append( "Локаль системы: "+locale.getCountry()+" ("+locale.getDisplayCountry()+") Установленные приложения:\n");
            for (int j = 0; j < allApplication.size(); j++){
                fileWriter.append(j+1+") Пакет: "+allApplication.get(j).packageName+" Название приложения: "+packageManager.getApplicationLabel(allApplication.get(j)).toString()+"\n");
            }
            fileWriter.close();
            fileWriter = null;
            allApplication.clear();
            allApplication = null;
        }
        catch (FileNotFoundException fnfe){
            Log.e(TAG, "Ошибка открытия файла: ", fnfe);
        } catch (IOException ioe) {
            Log.e(TAG, "Ошибка записи в файл: ", ioe);
        }

    }

    public void writeLog(String timeStart, String timeStop, String timeWork, String nameProcess, String nameApplication){
        try {
            FileWriter fileWriter = new FileWriter(Environment.getExternalStorageDirectory() + "/NewApplicationStart.log", true);
            if (timeStart != null){
                fileWriter.append("------------------------------\n");
                fileWriter.append(timeStart + " Запустили процесс: " + nameProcess + ". Название приложения: " + nameApplication + ".\n");
            }
            else if (timeStop != null){
                fileWriter.append(timeStop + " Закрыли процесс: " + nameProcess + ".\n");
                fileWriter.append("\t\t\t\t\t\t\t\tПриложение работало: " + timeWork + ".\n");
            }
            fileWriter.close();
            fileWriter = null;
        } catch (IOException ioe) {
            Log.e(TAG, "Ошибка при открытии файла лога: ", ioe);
        }


    }
}
