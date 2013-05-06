package com.KVP.ProcessMonitor;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

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
    LocationListener locationListener = null;
    double lat;
    double lot;
    public Server() {
        super("Server");
    }

    public void onCreate(){
        super.onCreate();
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        pattern = new SimpleDateFormat();
        pattern.applyPattern("dd-MM-yyyy HH-mm-ss");
        startForeground(1, new Notification());
        final LocationManager locationManager =  (LocationManager)this.getSystemService(LOCATION_SERVICE);
        final Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lot = location.getLongitude();
                Log.v(TAG, "Широта: " + location.getLatitude() + ". Долгота: " + location.getLongitude() + ".");try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lot, 1);
                    Address address = addresses.get(0);
                    Log.v(TAG, address.getAddressLine(0)+" "+address.getThoroughfare()+" "+address.getFeatureName() +" "+address.getSubAdminArea());
                    String locationInfo = "Широта: "+lat+". Долгота: \b"+lot+".\nАдрес: " + address.getSubAdminArea() + ", " + address.getAddressLine(0) + ", " + address.getAdminArea() +", " + address.getCountryName() + ".\nКод страны: " + address.getCountryCode();
                    Toast.makeText(getBaseContext(),locationInfo, Toast.LENGTH_LONG).show();
                } catch (IOException ioe) {
                    Log.e(TAG, "ERROR: " + ioe);
                } catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e);
                }
                locationManager.removeUpdates(locationListener);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
                try {
                    Log.v(TAG, "Provider disabled. Started...");
                    Intent startLocPropv = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startLocPropv.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getBaseContext().startActivity(startLocPropv);
                    Toast.makeText(getBaseContext(), "Please, enable provider network location.", Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    Log.e(TAG, "ERROR: " + e);
                }
            }
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0, locationListener);
    }




    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityManager activityManager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
        PackageManager packageManager = this.getPackageManager();
        ApplicationInfo ai = null;
        List <ActivityManager.RunningAppProcessInfo> listApplicationAll = null;
        List <ActivityManager.RunningAppProcessInfo> listApplicationNew = new ArrayList<ActivityManager.RunningAppProcessInfo>();
        List <Long> timeApplicationStart = new ArrayList <Long>();
        List <String> timeApplicationStartStr = new ArrayList<String>();
        Long timeStop;
        systemDevice(packageManager);
        while (work){
            listApplicationAll = activityManager.getRunningAppProcesses();
            if (listApplicationNew.isEmpty() == false)
            {
                for (int i = 0; i < listApplicationNew.size(); i++){
                    String nameApplication = listApplicationNew.get(i).processName;
                    for (int j = 0; j < listApplicationAll.size(); j++){
                        if (listApplicationAll.get(j).processName.equals(nameApplication) && listApplicationAll.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
                            calendar = Calendar.getInstance();
                            timeStop = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                            String timeWork = getTimeWork(timeApplicationStart.get(i), timeStop);
                            nameApplication = getApplicationName(packageManager, listApplicationNew.get(i).processName);
                            Log.v(TAG, "------------------------------------------------------------");
                            Log.v(TAG, timeApplicationStartStr.get(i)+" Запустили процесс: " + listApplicationNew.get(i).processName + ". Название приложения: " + nameApplication);
                            Log.v(TAG, pattern.format(calendar.getTime())+" Закрыт процесс: " + listApplicationNew.get(i).processName);
                            Log.v(TAG, "\t\t\t\t\t\t\t\tПроцесс: " + listApplicationNew.get(i).processName+" работал: "+timeWork);
                            Log.v(TAG, "============================================================");
                            //Log.v(TAG, "Статус приложения: " + listApplicationAll.get(j).importance);
                            writeLog(timeApplicationStartStr.get(i), pattern.format(calendar.getTime()).toString(), timeWork, listApplicationNew.get(i).processName, nameApplication);
                            listApplicationNew.remove(i);
                            timeApplicationStart.remove(i);
                            timeApplicationStartStr.remove(i);
                            Log.v("label. ", "for-1 Label - 1");
                            if (listApplicationNew.isEmpty())
                                break;
                        }else if (listApplicationAll.get(j).processName.equals(nameApplication) && listApplicationAll.get(j).importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            listApplicationAll.remove(j);
                            Log.v("label. ", "for-1 Label - 2");
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
                        Log.v("label. ", "for-2 Label - 1");
                    }
                    else {
                        Long timeStart = null;
                        calendar = Calendar.getInstance();
                        timeStart = Long.valueOf(calendar.get(Calendar.HOUR))*3600000 + Long.valueOf(calendar.get(Calendar.MINUTE))*60000 + Long.valueOf(calendar.get(Calendar.SECOND))*1000;
                        timeApplicationStart.add(0, timeStart);
                        timeApplicationStartStr.add(0, pattern.format(calendar.getTime()).toString());
                        listApplicationNew.add(0, listApplicationAll.get(j));
                        //Log.v(TAG, pattern.format(calendar.getTime()) + " Записали процесс: " + listApplicationAll.get(j).processName + " Название приложения: " + nameAppication);
                        //writeLog(pattern.format(calendar.getTime()).toString(), null, null, listApplicationAll.get(j).processName, nameAppication);
                        Log.v("label. ", "for-2 Label - 2");

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
            fileWriter.append("------------------------------------------------------------\n");
            fileWriter.append(timeStart + " Запустили процесс: " + nameProcess + ". Название приложения: " + nameApplication + ".\n");
            fileWriter.append(timeStop + " Закрыли процесс: " + nameProcess + ".\n");
            fileWriter.append("\t\t\t\t\t\t\t\tПриложение работало: " + timeWork + ".\n");
            fileWriter.append("============================================================\n");
            fileWriter.close();
            fileWriter = null;
        } catch (IOException ioe) {
            Log.e(TAG, "Ошибка при открытии файла лога: ", ioe);
        }
    }

    public String getApplicationName(PackageManager packageManager, String processName){
        ApplicationInfo ai = null;
        try {
            ai = (ApplicationInfo)getPackageManager().getApplicationInfo(processName, 0);
            processName = packageManager.getApplicationLabel(ai).toString();
        }
        catch (PackageManager.NameNotFoundException pmnnfe) {
            Log.e(TAG, "Ошибка при получении информации о пакете: "+ pmnnfe);
            processName = null;
        }
        return processName;
    }


}
