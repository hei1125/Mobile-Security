package com.example.testing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	public MyDBHelper dbHelper; 
    private SharedPreferences prefs;
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	dbHelper = new MyDBHelper(context); 
    	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");
        wl.acquire();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoBackup = prefs.getBoolean("auto_backup", false);
        boolean backupOverWifi = prefs.getBoolean("backup_wifi", false);
        if (isAutoBackup) {
        	if (backupOverWifi == false || backupOverWifi == usingWifi(context)){
        		autobackup(context);
        	}
        } 
        //Release the lock
        wl.release();
    }
    public void setAlarm(Context context)
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String interval = prefs.getString("auto_backup_interval","1");
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000 * Integer.parseInt(interval) , pi); 
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
    
    public boolean usingWifi(Context context){
    	// Initialize Network Info
    	ConnectivityManager cm =
    	    	        (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    	if (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting()){
    		if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
				return true;
			}
    	}
    	return false;
    }
    
    public boolean autobackup(Context context){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Auto Backup");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
		Toast.makeText(context, "Auto Backup", Toast.LENGTH_SHORT).show();
    	return true;
    }
}