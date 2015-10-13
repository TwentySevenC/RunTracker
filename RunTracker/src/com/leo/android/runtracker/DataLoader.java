package com.leo.android.runtracker;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
	private D mData;

	public DataLoader(Context context) {
		super(context);
	}

	
	@Override
	public void deliverResult(D data) {
		mData = data;
		
		/**
		 * if the loader started, invoke super method--deliverResult()
		 * deliver the data to main thread
		 */
		if(isStarted()){
			super.deliverResult(data);
		}
	}

	@Override
	protected void onStartLoading() {
		if(mData != null){
			deliverResult(mData);
		}else{
			forceLoad();
		}
	}

}
