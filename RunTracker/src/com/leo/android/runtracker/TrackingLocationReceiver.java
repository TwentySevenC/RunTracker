package com.leo.android.runtracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

public class TrackingLocationReceiver extends LocationReceiver {
	private static final String TAG = "TrackingLocationReceiver";

	@Override
	protected void onLocationReceived(Context context, Location loc) {
		// TODO Auto-generated method stub
		RunManager.get(context).insertLocation(loc);
		Log.d(TAG, TAG + " get a location.");
	}
	
}
