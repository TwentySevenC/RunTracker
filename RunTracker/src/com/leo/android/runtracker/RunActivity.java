package com.leo.android.runtracker;

import android.support.v4.app.Fragment;

public class RunActivity extends SingleFragmentActivity {
	public static final String EXTRA_RUN_ID = "com.leo.android.runtracker.run_id";

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.run, menu);
		return true;
	}*/

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		Long runId = getIntent().getLongExtra(EXTRA_RUN_ID, -1);
		if(runId != -1){
			
			return RunFragment.newInstance(runId);
			
		}else{
			
			return new RunFragment();
		}
	}

}
