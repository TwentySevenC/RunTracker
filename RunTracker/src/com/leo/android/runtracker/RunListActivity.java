package com.leo.android.runtracker;

import android.support.v4.app.Fragment;

public class RunListActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		// TODO Auto-generated method stub
		return new RunListFragment();
	}

}
