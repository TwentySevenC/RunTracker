package com.leo.android.runtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.android.runtracker.RunDataBaseHelper.RunCursor;

public class RunListFragment extends ListFragment implements LoaderCallbacks<Cursor>{
	private static final int REQUEST_NEW_RUN = 0;
//	private RunCursor mRunCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		/*mRunCursor = RunManager.get(getActivity()).queryRuns();
		
		//create an adapter
		RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), mRunCursor);
		setListAdapter(adapter);*/
		
		//Initialize the loader to load the list of runs
		getLoaderManager().initLoader(0, null, this);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
//		mRunCursor.close();
		
		super.onDestroy();
		
	}
	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		
		inflater.inflate(R.menu.run_list_options, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_item_new_run:
			Intent intent = new Intent(getActivity(), RunActivity.class);
			startActivityForResult(intent, REQUEST_NEW_RUN);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
		
	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
		if(REQUEST_NEW_RUN == requestCode){
			/*mRunCursor.requery();                                                          *//**refresh data*//*
			RunCursorAdapter adapter = (RunCursorAdapter) getListAdapter();               *//**update data*//*
			adapter.notifyDataSetChanged();*/     
			
			//Restart the loader to get any new run available
			getLoaderManager().restartLoader(0, null, this);
		}
		
	}



	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(getActivity(), RunActivity.class);
		intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
		startActivity(intent);
	}



	/**
	 * 
	 * define a inner class extending CursorAdaptor
	 *
	 */
	private static class RunCursorAdapter extends CursorAdapter{
		private RunCursor mCursor;

		public RunCursorAdapter(Context context, Cursor cursor) {
			super(context, cursor, 0);
			// TODO Auto-generated constructor stub
			mCursor = (RunCursor)cursor;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			TextView cellView = (TextView)view;
			String cellString = context.getString(R.string.cell_text, mCursor.getRun().getStartDate());
			cellView.setText(cellString);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
		}
		
	}
	
	/**
	 * 
	 * a cursor loader for Loading run list.. 
	 *
	 */
	private static class RunListCursorLoader extends SQLiteCursorLoader {

		public RunListCursorLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor loadCursor() {
			//query list of runs
			return RunManager.get(getContext()).queryRuns();
		}
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		// TODO Auto-generated method stub
		return new RunListCursorLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// Create an adapter to point at this cursor
		RunCursorAdapter adapter = new RunCursorAdapter(getActivity(), (RunCursor)cursor);
		setListAdapter(adapter);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// stop using the cursor  (via the adapter)
		setListAdapter(null);
		
	}
		
}
