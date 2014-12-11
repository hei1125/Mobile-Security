package com.example.testing;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver
{
	public MyDBHelper dbHelper; 
    private SQLiteDatabase db; 
    private SharedPreferences prefs;
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	dbHelper = new MyDBHelper(context); 
    	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        wl.acquire();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoBackup = prefs.getBoolean("auto_backup", false);
        if (isAutoBackup) {
        	autobackup(context);
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
    
    public boolean autobackup(Context context){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Auto Backup");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
		Toast.makeText(context, "Auto Backup", Toast.LENGTH_SHORT).show();
    	return true;
    }
}