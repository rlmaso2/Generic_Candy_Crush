package com.javadude.todo;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class TodoContentProvider extends ContentProvider {
	// Database Constants
	private static final String TODO_TABLE = "todo";
	public static final String ID = "_id"; // NOTE THE UNDERSCORE!
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String DONE = "done";  // added in version 2
	public static final String PRIORITY = "priority";  // added in version 3
	public static final int DB_VERSION = 3;
	
	private static class OpenHelper extends SQLiteOpenHelper {
		public OpenHelper(Context context) {
			super(context, "TODO", null, DB_VERSION);
		}
		@Override public void onCreate(SQLiteDatabase db) {
			try {
				db.beginTransaction();
				// always keep version 1 creation
				String sql = String.format(
						"create table %s (%s integer primary key autoincrement, %s text, %s text)", 
						TODO_TABLE, ID, NAME, DESCRIPTION);
				db.execSQL(sql);
				onUpgrade(db, 1, DB_VERSION);  // run the upgrades starting from version 1
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// UPGRADE REALLY SHOULD SAVE EXISTING DATA IF AT ALL POSSIBLE
			//   - use "alter table" where possible
			// The following example assumes we're up to version 3
			
			try {
				db.beginTransaction();
				switch(oldVersion) {
					default:
						throw new IllegalStateException("Unexpected existing database version " + oldVersion);

					case 1:
						// do upgrades from version 1 -> version 2
						db.execSQL(String.format("alter table %s add %s integer", TODO_TABLE, DONE));
						// FALL THROUGH TO APPLY FURTHER UPGRADES
					case 2:
						// do upgrades from version 2 -> version 3
						db.execSQL(String.format("alter table %s add %s text", TODO_TABLE, PRIORITY));
						// FALL THROUGH TO APPLY FURTHER UPGRADES
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}

	private SQLiteDatabase db;

	@Override
	public boolean onCreate() {
		db = new OpenHelper(getContext()).getWritableDatabase();
		return true; // data source opened ok!
	}	
	
	// URI Constants
	public static final int TODOS = 1;
	public static final int TODO_ITEM = 2;
	public static final String AUTHORITY = "com.javadude.todo";
	public static final String BASE_PATH = "todo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.javadude.todo";
	public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.javadude.todo";	
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH, TODOS);
			// if we see content://com.javadude.todo/todo -> return TODOS (1)
		URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", TODO_ITEM);
		    // if we see content://com.javadude.todo/todo/42 -> return TODO_ITEM (2)
	}

	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, 
							String sortOrder) {
		switch (URI_MATCHER.match(uri)) {
			case TODOS: {
				// get all TODOS
				Cursor c = db.query(TODO_TABLE, 
								projection, 
								selection, 
								selectionArgs, 
								null, null, // groupby, having 
								sortOrder);
				
				// NOTE: we must set the notification URI on the cursor
				//       so it can listen for changes that we might make!
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			}
			case TODO_ITEM: {
				// get specific TODO
				String id = uri.getLastPathSegment();
				Cursor c = db.query(TODO_TABLE, 
						projection, 
						ID + "=?", // DO NOT simply say ID + "=" + id! SQL INJECTION!
						new String[] {id}, 
						null, null, // groupby, having 
						sortOrder);
				// NOTE: we must set the notification URI on the cursor
				//       so it can listen for changes that we might make!
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			}
			default:
				return null; // unknown
		}
	}
	
	@Override
	public String getType(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case TODOS:
				return CONTENT_DIR_TYPE;
			case TODO_ITEM:
				return CONTENT_ITEM_TYPE;
			default:
				return null; // unknown
		}
	}
	
	@Override public Uri insert(Uri uri, ContentValues values) {
		long id = db.insert(TODO_TABLE, null, values);
		// the null is the "null column hack"
		//   -- if inserting an empty row, specify the name of a nullable column
		Uri insertedUri = Uri.withAppendedPath(CONTENT_URI, ""+id);
		getContext().getContentResolver().notifyChange(insertedUri, null);
		return insertedUri;
	}

	@Override public int delete(Uri uri, String selection, String[] selectionArgs) {
		int numDeleted = db.delete(TODO_TABLE, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numDeleted;
	}

	@Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int numUpdated = db.update(TODO_TABLE, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return numUpdated;
	}
	
		// static helper methods
	public static TodoItem findTodo(Context context, long id) {
		Uri uri = Uri.withAppendedPath(CONTENT_URI, "" + id);
		String[] projection = {ID, NAME, DESCRIPTION, PRIORITY};
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().
					query(uri, projection, ID + "=" + id, null, NAME + " ASC");
			if (cursor == null || !cursor.moveToFirst())
				return null;
			return new TodoItem(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
		} finally {
			// BE SURE TO CLOSE THE CURSOR!!!
			if (cursor != null)
				cursor.close();
		}
	}
	public static void updateTodo(Context context, TodoItem todo) {
		Uri uri = Uri.withAppendedPath(CONTENT_URI, "" + todo.getId());
		ContentValues values = new ContentValues();
		values.put(NAME, todo.getName());
		values.put(DESCRIPTION, todo.getDisplayName());
		values.put(PRIORITY, todo.getPriority());
		if (todo.getId() == -1) {
			Uri insertedUri = context.getContentResolver().insert(uri, values);
			String idString = insertedUri.getLastPathSegment();
			long id = Long.parseLong(idString);
			todo.setId(id);
		} else {
			context.getContentResolver().update(uri, values, ID + "=" + todo.getId(), null);
		}
	}
}
