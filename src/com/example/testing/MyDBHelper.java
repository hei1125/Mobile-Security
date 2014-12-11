package com.example.testing;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper {
	 
    public static final String DATABASE_NAME = "log.db";
    private final static String _TableName = "LogOperation";
    public static final int VERSION = 1;    
 
    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "( " +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
	            "Operation VARCHAR(50), " +
                "Time VARCHAR(50)" +
	            ");";
    	db.execSQL(SQL);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    	final String SQL = "DROP TABLE " + _TableName;
    	db.execSQL(SQL);    
    }
 
}
