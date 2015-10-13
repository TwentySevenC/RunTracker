package com.leo.android.runtracker;


import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class RunFragment extends Fragment {
	private static final String TAG = "RunFragment";
	private static final String ARG_RUN_ID = "RUN_ID";
	private static final int LOAD_RUN = 0;
	private static final int LOAD_LOCATION = 1;
	
	private Button mStartButton, mStopButton;
	private TextView mStartedTextView, mLatitudeTextView, mLongitudeTextView,
					mAltitudeTextView, mDurationTextView;
	private RunManager mRunManager;
	
	private Run mRun;
	private Location mLastLocation;
	
	/**
	 * register a new receiver, keep the receiver alive between Activity's OnStart()~OnStop()
	 * 
	 * life cycle. Update the location
	 */
	private LocationReceiver mLocationReceiver = new LocationReceiver(){

		@Override
		protected void onLocationReceived(Context context, Location loc) {
			// TODO Auto-generated method stub
			if(mRunManager.isTrackingRun(mRun))
				return ;
			mLastLocation = loc;
			Log.d(TAG, "Received a new last location!");
			if(isVisible()){
				updateUI();              /**if this fragment is visible, update data*/
			}
		}

		@Override
		protected void onProviderEnabledChanged(boolean enabled) {
			// TODO Auto-generated method stub
			int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
			Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
		}
		
	};
	
	/**
	 * create a fragment
	 * @param runId
	 * @return
	 */
	public static Fragment newInstance(long runId){
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		RunFragment rf = new RunFragment();
		rf.setArguments(args);
		return rf;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mRunManager = RunManager.get(getActivity());
		
		Bundle args = getArguments();
		if(args != null){
			long runId = args.getLong(ARG_RUN_ID, -1);
			
			if(runId != -1){
				Log.d(TAG, "Run id: " + runId);
//				mRun = mRunManager.getRun(runId);                             // get the run instance
//				mLastLocation = mRunManager.getLastLocationForRun(runId);     // get the last location for a specify runId
				LoaderManager lm = getLoaderManager();
				lm.initLoader(LOAD_RUN, args, new RunLoaderCallbacks());
				lm.initLoader(LOAD_LOCATION, args, new LocationLoaderCallBacks());
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_run, container, false);
		
		mRunManager = RunManager.get(getActivity());
		
		mAltitudeTextView = (TextView)view.findViewById(R.id.run_altitudeTextView);
		mStartedTextView = (TextView)view.findViewById(R.id.run_startedTextView);
		mLongitudeTextView = (TextView)view.findViewById(R.id.run_longitudeTextView);
		mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);
		mLatitudeTextView = (TextView)view.findViewById(R.id.run_latitudeTextView);
		
		mStartButton =(Button)view.findViewById(R.id.run_start);
		mStopButton =(Button)view.findViewById(R.id.run_stop);
		
		mStartButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				mRunManager.startLocationUpdates();
				if(mRun == null){
					/**record started time */
					mRun = mRunManager.startNewRun();
				}else{
					mRunManager.startTrackingRun(mRun);
				}
				updateUI();
			}
		});
		
		mStopButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				mRunManager.stopLocationUpdates();
				mRunManager.stopLocationUpdates();
				updateUI();
			}
		});
		updateUI();
		
		return view;
	}
	
	public void updateUI() {

		boolean started = mRunManager.isTrackingRun();
		boolean trackingThisRun = mRunManager.isTrackingRun(mRun);

		if(null != mRun){
			mStartedTextView.setText(mRun.getStartDate().toString());
		}

		int durationSeconds = 0;
		if (mLastLocation != null && mRun != null) {
			durationSeconds = mRun.durationSeconds(mLastLocation.getTime());
			/** System.currentTimeMillis() */
			mAltitudeTextView.setText(String.valueOf(mLastLocation
					.getAltitude()));
			mLatitudeTextView.setText(String.valueOf(mLastLocation
					.getLatitude()));
			mLongitudeTextView.setText(String.valueOf(mLastLocation
					.getLongitude()));
		}

		mDurationTextView.setText(Run.formatDuration(durationSeconds));
		mStartButton.setEnabled(!started);
		mStopButton.setEnabled(started && trackingThisRun);

	}
	
	
	

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		getActivity().registerReceiver(mLocationReceiver,               /**register location receiver */
				new IntentFilter(RunManager.ACTION_LOCATION));
		super.onStart();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		getActivity().unregisterReceiver(mLocationReceiver);           /**unregister location receiver*/
		super.onStop();
	}
	
	
	//---------------------------------------
	private class RunLoaderCallbacks implements LoaderCallbacks<Run>{

		@Override
		public Loader<Run> onCreateLoader(int id, Bundle args) {
			return new RunLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}

		@Override
		public void onLoadFinished(Loader<Run> loader, Run run) {

			mRun = run;
			updateUI();
			
		}

		@Override
		public void onLoaderReset(Loader<Run> arg0) {
				//do nothing 
		}
		
	}
	
	
	private class LocationLoaderCallBacks implements LoaderCallbacks<Location>{
		
		@Override
		public Loader<Location> onCreateLoader(int id, Bundle args) {
			
			return new LastLocationLoader(getActivity(), args.getLong(ARG_RUN_ID));
		}

		@Override
		public void onLoadFinished(Loader<Location> loader, Location loc) {
			mLastLocation = loc;
			updateUI();
			
		}

		@Override
		public void onLoaderReset(Loader<Location> loader) {
			// do nothing
			
		}
		
	}
	
}
