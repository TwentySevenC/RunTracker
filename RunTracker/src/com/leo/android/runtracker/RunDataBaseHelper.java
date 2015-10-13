package com.leo.android.runtracker;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

public class RunDataBaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "runs.sqlite";
	private static final int VERSION = 1;

	private static final String TABLE_RUN = "run";
	private static final String COLUMN_RUN_ID = "_id";
	private static final String COLUMN_RUN_START_DATE = "start_date";
	
	private static final String TABLE_LOCATION = "location";
	private static final String COLUMN_LOCATION_LATITUDE = "latitude";
	private static final String COLUMN_LOCATION_LONGITUDE = "longitude";
	private static final String COLUMN_LOCATION_ALTITUDE = "altitude";
	private static final String COLUMN_LOCATION_TIMESTAMP = "time_stamp";
	private static final String COLUMN_LOCATION_PROVIDER = "provider";
	private static final String COLUMN_LOCATION_RUN_ID = "run_id";
	
	public RunDataBaseHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		//create "run" table
		database.execSQL("CREATE TABLE run (_id INTEGER PRIMARY KEY AUTOINCREMENT, start_date INTEGER)");
		//create "location" table
		database.execSQL("CREATE TABLE location (time_stamp INTEGER, latitude REAL, " +
				"longitude REAL, altitude REAL, provider VARCHAR(100), " +
				"run_id INTEGER  REFERENCES run(_id))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	//insert a run instance
	public long insertRun(Run run){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		return getWritableDatabase().insert(TABLE_RUN, null, cv);
	}
	
	public long insertLocation(long runId, Location location){
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LOCATION_ALTITUDE, location.getAltitude());
		cv.put(COLUMN_LOCATION_LATITUDE, location.getLatitude());
		cv.put(COLUMN_LOCATION_LONGITUDE, location.getLongitude());
		cv.put(COLUMN_LOCATION_PROVIDER, location.getProvider());
		cv.put(COLUMN_LOCATION_TIMESTAMP, location.getTime());
		cv.put(COLUMN_LOCATION_RUN_ID, runId);
		return getWritableDatabase().insert(TABLE_LOCATION, null, cv);
	}
	
	public RunCursor queryRuns(){
		//select * from run ordered by started time asc
		Cursor wrapped = getReadableDatabase()
				.query(TABLE_RUN, null, null, null, null, null, COLUMN_RUN_START_DATE + " asc");
		return new RunCursor(wrapped);
	}
	
	
	public RunCursor queryRun(long id){
		//select * from run where run_id = id
		Cursor wrapped = getReadableDatabase()
				.query(TABLE_RUN, 
						null, 
						COLUMN_RUN_ID + " = ?", 
						new String[]{String.valueOf(id)}, 
						null, 
						null, 
						null,
						"1"); //limit one row
		
		return new RunCursor(wrapped);
	}
	
	public LocationCursor queryLastLocationForRun(long runId){
		//select * from location where run_id = id
		Cursor wrapped = getReadableDatabase().query(TABLE_LOCATION, 
													null, 
													COLUMN_LOCATION_RUN_ID + " = ?", 
													new String[]{String.valueOf(runId)}, 
													null, 
													null, 
													COLUMN_LOCATION_TIMESTAMP + " desc", 
													"1");
		
		return new LocationCursor(wrapped);
	}
	
	/**
	 * A convenience class to wrap a cursor that returns rows from the "run" table
	 * {getRun method} convert a cursor row to a Run instance
	 */
	public static class RunCursor extends CursorWrapper{

		public RunCursor(Cursor cursor) {
			super(cursor);
		}
		
		/**
		 * Returns a Run object configured for the current row
		 * or null if the current row is invalid 
		 */
		public Run getRun(){
			if(isAfterLast() || isBeforeFirst()){
				return null;
			}
			
			Run run = new Run();
			long id = getLong(getColumnIndex(COLUMN_RUN_ID));
			run.setId(id);
			
			long startedTime = getLong(getColumnIndex(COLUMN_RUN_START_DATE));
			run.setStartDate(new Date(startedTime));
			
			return run;
		}
	}
	
	/**
	 * 
	 * A convenience class to wrap a cursor that returns rows from the "location" table
	 * convert a cursor row to a location instance
	 *
	 */
	public static class LocationCursor extends CursorWrapper {

		public LocationCursor(Cursor cursor) {
			super(cursor);
		}
		
		/**
		 * Return a location object configured for the current row or null if the current row is invalid
		 */
		public Location getLocation(){
			if(isAfterLast() || isBeforeFirst())
			return null;
			
			String provider = getString(getColumnIndex(COLUMN_LOCATION_PROVIDER));
			Location location = new Location(provider);
			
			location.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_ALTITUDE)));
			location.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_LATITUDE)));
			location.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_LONGITUDE)));
			location.setTime(getColumnIndex(COLUMN_LOCATION_TIMESTAMP));
			
			return location;
		}
	}

}
