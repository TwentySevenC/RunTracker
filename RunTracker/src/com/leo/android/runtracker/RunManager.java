package com.leo.android.runtracker;

import com.leo.android.runtracker.RunDataBaseHelper.LocationCursor;
import com.leo.android.runtracker.RunDataBaseHelper.RunCursor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class RunManager {
	private static final String TAG = "RunManager";
	private static final String PREFS_FILE = "runs";
	private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";
	
	public static final String ACTION_LOCATION =
			"com.leo.android.runtracker.ACTION_LOCATION";
	
	private static RunManager sRunManager;
	private Context mAppContext;
	private LocationManager mLocationManager;
	private RunDataBaseHelper mHelper;
	private SharedPreferences mPrefs;
	private long mCurrentRunId;
	
	//private constructor
	private RunManager(Context appContext){
		mAppContext = appContext;
		mLocationManager = (LocationManager)appContext.getSystemService(Context.LOCATION_SERVICE);
		mHelper = new RunDataBaseHelper(appContext);
		mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
		mCurrentRunId = mPrefs.getLong(PREF_CURRENT_RUN_ID, -1);
		}
	
	//get a single RunManager instance
	public static RunManager get(Context c){
		if(sRunManager == null){
			sRunManager = new RunManager(c.getApplicationContext());
		}
		return sRunManager;			
	}
	
	private PendingIntent getLocationPendingIntent(boolean shouldCreate){
		Intent broadcast = new Intent(ACTION_LOCATION);
		int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
		return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
	}
	
	public void startLocationUpdates(){
		String provider = LocationManager.GPS_PROVIDER;
		
		/**get the last known location and broadcast it if it has*/
		Location lastKnownLoc = mLocationManager.getLastKnownLocation(provider);
		if(lastKnownLoc != null ){
			Log.d(TAG, "Last location: " + lastKnownLoc);
			
			lastKnownLoc.setTime(System.currentTimeMillis());
			broadcastLocation(lastKnownLoc);
		}
		
		//start update from the locationManager
		PendingIntent pi = getLocationPendingIntent(true);
		mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
	}
	
	public void stopLocationUpdates(){
		PendingIntent pi = getLocationPendingIntent(false);
		if(null != pi){
			mLocationManager.removeUpdates(pi);
			pi.cancel();
		}
	}
	
	/**
	 * is there a new run that tracked now ?
	 * @return
	 */
	public boolean isTrackingRun(){
		return getLocationPendingIntent(false) != null;
	}
	
	
	/**
	 * judge whether run(parameter) is the current run or not
	 * @param run
	 * @return
	 */
	public boolean isTrackingRun(Run run){
		return run != null && run.getId() == mCurrentRunId;
	}
	
	/**Broadcast the last location*/
	private void broadcastLocation(Location loc){
		Intent broadcast = new Intent(ACTION_LOCATION);
		broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, loc);
		mAppContext.sendBroadcast(broadcast);
	}
	
	/**
	 * Start a new run
	 * @return
	 */
	public Run startNewRun(){
		//Insert a new run to db
		Run run = insertRun();
		//start tracking the run 
		startTrackingRun(run);
		return run;
	}
	
	public void insertLocation(Location loc){
		if(mCurrentRunId != -1){
			mHelper.insertLocation(mCurrentRunId, loc);
		}else{
			Log.e(TAG, "Location received with no tracking run; ignoring.");
		}
	}
	
	public RunCursor queryRuns(){
		return mHelper.queryRuns();
	}
	

	/**
	 * get a Run instance 
	 * @param id run_id
	 * @return
	 */
	public Run getRun(long runId){
		Run run = null;
		
		RunCursor cursor = mHelper.queryRun(runId);
		cursor.moveToFirst();
		
		if(!cursor.isAfterLast()){
			run = cursor.getRun();
		}
		cursor.close();
		return run;
	}
	
	/**
	 * get the last location for a specify runId
	 * @param runId
	 * @return
	 */
	public Location getLastLocationForRun(long runId){
		Location location = null;
		
		LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
		cursor.moveToFirst();
		
		if(cursor.isAfterLast()){
			location = cursor.getLocation();
		}
		
		cursor.close();
		return location;
	}
	
	private Run insertRun(){
		Run run = new Run();
		run.setId(mHelper.insertRun(run));
		return run;
	}
	
	public void startTrackingRun(Run run){
		//Keep the ID
		mCurrentRunId = run.getId();
		
		Log.d(TAG, "Current Run ID: " + mCurrentRunId);
		
		//store current id in sharedPreferences
		mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();
		//start location updates
		startLocationUpdates();
	}
	
	public void stopRun(){
		stopLocationUpdates();
		mCurrentRunId = -1;
		mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
	}
}
