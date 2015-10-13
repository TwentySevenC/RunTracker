package com.leo.android.runtracker;

import android.annotation.SuppressLint;
import java.util.Date;
import java.util.Locale;



public class Run {
	private Date mStartDate;
	private long mId;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public Run(){
		mId = -1;
		mStartDate = new Date();
	}
	
	public Date getStartDate() {
		return mStartDate;
	}

	public void setStartDate(Date startDate) {
		mStartDate = startDate;
	}
	
	public int durationSeconds(long endMillis){
		return (int)(endMillis - mStartDate.getTime())/1000;
	}
	
	@SuppressLint("DefaultLocale")
	public static String formatDuration(int durationSeconds){
		int seconds = durationSeconds % 60;
		int minutes = ((durationSeconds - seconds) /60) % 60;
		int hours = (durationSeconds - minutes * 60 - seconds) / 3600;
		return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
	}
	
}
