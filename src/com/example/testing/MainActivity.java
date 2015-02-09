package com.example.testing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public MyDBHelper dbHelper; 
    private SQLiteDatabase db; 	
	private AlarmReceiver alarm;
	Cursor cursor;
    ArrayList<String> vCard;
    String vfile;
    GPSTracker gps;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        
        // Initialize the log DB
        dbHelper = new MyDBHelper(this); 
        
        // Initialize the alarm
        alarm = new AlarmReceiver();
        startRepeatingTimer();
    }


    public void startRepeatingTimer() {
       	Context context = this.getApplicationContext();
       	if (alarm != null) {
       		alarm.setAlarm(context);
       	} else {
       		Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
       	}
    }
    
    public void cancelRepeatingTimer(){
    	Context context = this.getApplicationContext();
    	if (alarm != null) {
    		alarm.cancelAlarm(context);
    	} else {
    		Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
    	}
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
    	 Fragment fragment = null;
        // update the main content by replacing fragments
    	switch(position) {
   	 		// Backup
    		case 0:
    			fragment = new BackupFragment();
    			break;
    			// Missing Device
    		case 1:
    			fragment = new RestoreFragment();
    			break;
    			// Management
    		case 2:
    			fragment = new NetworkFragment();
    			break;
    		case 3:
    			fragment = new LogFragment();
    			break;
    		case 4:
    			fragment = new SettingFragment();
    			break;
    		default:
    			break;
        }
    	FragmentManager fragmentManager = getFragmentManager();
    	fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.action_settings);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	
        // int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Backup Contacts
     */   
    private void getVcardString() throws IOException {
        // TODO Auto-generated method stub
        vCard = new ArrayList<String>(); 
        vfile = "Contacts" + "_" + System.currentTimeMillis() + ".vcf";
        cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        if(cursor != null && cursor.getCount() > 0)
        {
            int i;
            String storage_path = Environment.getExternalStorageDirectory().toString() + File.separator + vfile;
            FileOutputStream mFileOutputStream = new FileOutputStream(storage_path, false);
            cursor.moveToFirst();
            for(i = 0;i<cursor.getCount();i++)
            {
                get(cursor);
                Log.d("TAG", "Contact "+(i+1)+"VcF String is"+ vCard.get(i));
                cursor.moveToNext();
                mFileOutputStream.write(vCard.get(i).toString().getBytes());
            }
            mFileOutputStream.close();
            cursor.close();
        }
        else
        {
            Log.d("TAG", "No Contacts in Your Phone");
        }
    }
    
    private void get(Cursor cursor2) {
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        AssetFileDescriptor fd;
        try {
            fd = this.getContentResolver().openAssetFileDescriptor(uri, "r");

            FileInputStream fis = fd.createInputStream();
            byte[] buf = new byte[(int) fd.getDeclaredLength()];
            fis.read(buf);
            String vcardstring= new String(buf);
            vCard.add(vcardstring);
        } catch (Exception e1) 
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    public boolean backupContact(){
    	try {
			getVcardString();
			Toast.makeText(this, "Backup Contacts is successful", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Backup Contact");  
        values.put("Time", System.currentTimeMillis());    
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
    	return true;
    }
    
    public boolean backupPhoto(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Backup Photo");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
		Toast.makeText(this, "Backup Photos", Toast.LENGTH_SHORT).show();
    	return true;
    }
    
    public boolean backupCall(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Backup Call History");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
        Toast.makeText(this, "Backup Calls", Toast.LENGTH_SHORT).show();
    	return true;
    }
    
    public boolean backupAll(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Backup All");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
		Toast.makeText(this, "Backup All", Toast.LENGTH_SHORT).show();
    	return true;
    }
    
    /**
     * Restore Methods
     */
    public boolean restoreContact(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Restore Contact");  
        values.put("Time", System.currentTimeMillis());    
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
    	return true;
    }
    
    public boolean restorePhoto(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Restore Photo");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
    	return true;
    }
    
    public boolean restoreCall(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Restore Call History");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
    	return true;
    }
    
    public boolean restoreAll(){
    	ContentValues values = new ContentValues(); 
        values.put("Operation", "Restore All");  
        values.put("Time", System.currentTimeMillis());  
        dbHelper.getWritableDatabase().insert("LogOperation", null, values);
    	return true;
    }
    
    public void clearHistory(){
        db = openOrCreateDatabase("log.db", Context.MODE_PRIVATE, null);    	
    	db.execSQL("DELETE FROM LogOperation;");
    	db.execSQL("VACUUM;");
    }
    /**
     * Backup fragment
     */
    public class BackupFragment extends Fragment {

    	public BackupFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        	Button backup_contacts = (Button)rootView.findViewById(R.id.backup_contacts);
        	Button backup_photos = (Button)rootView.findViewById(R.id.backup_photos);
        	Button backup_calls = (Button)rootView.findViewById(R.id.backup_calls);
        	Button backup_all = (Button)rootView.findViewById(R.id.backup_all);
        	backup_contacts.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				backupContact();
    			} 
    		});
        	backup_photos.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				backupPhoto();
    			} 
    		});
        	backup_calls.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				backupCall();
    				Toast.makeText(getActivity(), "Backup Call History", Toast.LENGTH_SHORT).show();
    			} 
    		});
        	backup_all.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				backupAll();
    				Toast.makeText(getActivity(), "Backup All", Toast.LENGTH_SHORT).show();
    			} 
    		});
            return rootView;
        }
    }
    /**
     * Restore fragment
     */
    public class RestoreFragment extends Fragment {
        public RestoreFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(R.layout.fragment_2, container, false);
        	Button restore_contacts = (Button)rootView.findViewById(R.id.restore_contacts);
        	Button restore_photos = (Button)rootView.findViewById(R.id.restore_photos);
        	Button restore_calls = (Button)rootView.findViewById(R.id.restore_calls);
        	Button restore_all = (Button)rootView.findViewById(R.id.restore_all);
        	restore_contacts.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				restoreContact();
    				Toast.makeText(getActivity(), "Restore Contact", Toast.LENGTH_SHORT).show();
    			} 
    		});
        	restore_photos.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				restorePhoto();
    				Toast.makeText(getActivity(), "Restore Photos", Toast.LENGTH_SHORT).show();
    			} 
    		});
        	restore_calls.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				restoreCall();
    				Toast.makeText(getActivity(), "Restore Call History", Toast.LENGTH_SHORT).show();
    			} 
    		});
        	restore_all.setOnClickListener(new Button.OnClickListener(){
    			@Override
				public void onClick(View v) {
    				// TODO Auto-generated method stub
    				restoreAll();
    				Toast.makeText(getActivity(), "Restore All", Toast.LENGTH_SHORT).show();
    			} 
    		});
            return rootView;
        }
    }
    /**
     * Location fragment
     */
    public class NetworkFragment extends Fragment {
		public NetworkFragment() {
        }
		
		public void checkLocation(View view){
        	TextView textView3 = (TextView)view.findViewById(R.id.textView3);
        	gps = new GPSTracker(MainActivity.this);

            // Check if GPS enabled
            if(gps.canGetLocation()) {

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                // \n is for new line
                textView3.setText("Your Location is - \nLat: " + latitude + "\nLong: " + longitude);
            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                gps.showSettingsAlert();
            }
		}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(R.layout.fragment_3, container, false);
        	checkLocation(rootView);
            return rootView;
        }
      	
    }
    /**
     * Log Operation fragment
     */
    public class LogFragment extends Fragment {
    	 
        public LogFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
        	View rootView = inflater.inflate(R.layout.fragment_4, container, false);
        	ArrayList<HashMap<String, Object>> listData = fillList();  
            SimpleAdapter adapter = fillAdapter(listData);  
            ((ListView) rootView.findViewById(R.id.listView1)).setAdapter(adapter); 
            return rootView;
        }
        
        public ArrayList<HashMap<String, Object>> fillList(){  
           ArrayList<HashMap<String, Object>> dataList = new ArrayList<HashMap<String, Object>>();             
           SQLiteDatabase db = dbHelper.getReadableDatabase();  
           int list_icon = R.drawable.list;
           

             
           try{  
               Cursor cursor = db.rawQuery("SELECT * FROM LogOperation ORDER by Time DESC", null);   
               while(cursor.moveToNext()) {    
            	   String Operation = cursor.getString(cursor.getColumnIndex("Operation"));    
                   Long Time = cursor.getLong(cursor.getColumnIndex("Time"));   
                   SimpleDateFormat formatter = new SimpleDateFormat("yyyy¶~MM§Îdd§ÈHH:mm:ss");

                   Date curDate = new Date(Time) ; 

                   String str = formatter.format(curDate);

                    
                   HashMap<String, Object> map = new HashMap<String, Object>();    
                   map.put("List_icon", Integer.toString(list_icon)); 
                   map.put("Operation",Operation);    
                   map.put("Time", str);    
                   dataList.add(map);    
               }    
           } catch (Exception ex){  
               ex.printStackTrace();  
           } finally {  
               if(db.isOpen()){  
                   db.close();  
               }  
           }      
           return dataList;  
       }  
       public SimpleAdapter fillAdapter(ArrayList<HashMap<String, Object>> listData){  
           SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,  
                                                       listData,
                                                       R.layout.list,    
                                                       new String[] {"List_icon","Operation", "Time"},        
                                                       new int[] {R.id.list, R.id.operation,R.id.time});    
             
           return adapter;    
       }   
    }
    /**
     * Setting Fragment
     */
    public class SettingFragment extends PreferenceFragment  {
        public SettingFragment() {
        }
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.layout.preferences);
            Preference myPref = (Preference) findPreference("clear_history");
            myPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                         public boolean onPreferenceClick(Preference preference) {
                        	clearHistory();
                        	Toast.makeText(getActivity(), "Clear History", Toast.LENGTH_SHORT).show();
							return true;
                         }
                     });
        }
       
    }
}
