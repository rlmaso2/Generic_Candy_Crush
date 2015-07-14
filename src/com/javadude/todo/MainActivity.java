package com.javadude.todo;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity {
	
	private static final int TODO_LOADER = 1;
	private SimpleCursorAdapter cursorAdapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ListView listView = (ListView) findViewById(R.id.listView1);
		
		String[] from = {
				TodoContentProvider.ID,
				TodoContentProvider.NAME,
				TodoContentProvider.DESCRIPTION
		};
		int[] to = {
				-1, // id not displayed in the layout
				R.id.text1,
				R.id.text2
		};

		cursorAdapter = new SimpleCursorAdapter(this, R.layout.odd, null, from, to, 0);
		listView.setAdapter(cursorAdapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(MainActivity.this, EditActivity.class);
				intent.putExtra("todoItemId", id);
				startActivity(intent);
			}
		});
		
		// start asynchronous loading of the cursor
        getSupportLoaderManager().initLoader(TODO_LOADER, null, loaderCallbacks);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_new:
				// bring up the edit activity
				Intent intent = new Intent(this, EditActivity.class);
				intent.putExtra("todoItemId", (long) -1);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

    private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
			String[] projection = {
					TodoContentProvider.ID, 
					TodoContentProvider.NAME, 
					TodoContentProvider.DESCRIPTION};
			return new CursorLoader(
					MainActivity.this, 
					TodoContentProvider.CONTENT_URI, 
					projection, 
					null, null, // groupby, having
					TodoContentProvider.NAME + " asc");
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			cursorAdapter.swapCursor(cursor); // set the data
		}

		@Override
		public void onLoaderReset(Loader<Cursor> cursor) {
			cursorAdapter.swapCursor(null); // clear the data
		}
	};
}

