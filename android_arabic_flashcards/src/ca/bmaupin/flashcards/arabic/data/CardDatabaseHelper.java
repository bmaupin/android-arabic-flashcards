package ca.bmaupin.flashcards.arabic.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class CardDatabaseHelper extends SQLiteOpenHelper {
	// tag for log messages
	private static final String TAG = "DatabaseHelper";
	
	public static final String DATABASE_NAME = "cards.db";
	public static final int DATABASE_VERSION = 19;
	public static final String CARDS_TABLE = "cards";
	public static final String AWS_CHAPTERS_TABLE = "aws_chapters";

	// The name of each column in the database
    public static final String _ID = BaseColumns._ID;
    
    public static final String CARDS_ARABIC = "arabic";
    public static final String CARDS_CATEGORY = "category";
    public static final String CARDS_ENGLISH = "english";
    public static final String CARDS_GENDER = "gender";
    public static final String CARDS_PLURAL = "plural";
    public static final String CARDS_PART = "part";
    
    public static final String AWS_CHAPTERS_CHAPTER = "chapter";
    public static final String AWS_CHAPTERS_CARD_ID = "card_ID";
    
    public static final String COUNT = "COUNT()";
    public static final String RANDOM = "RANDOM()";
	
	// variable to tell us whether onCreate() or onUpgrade() has been called
	boolean dbNeedsRefreshing = false;
	
	private final Context myContext;
	private SQLiteDatabase mDatabase;
	
	public CardDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.myContext = context;
	}
	
	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		// this will create a database if one doesn't already exist
		mDatabase = super.getReadableDatabase();
		
		Log.d(TAG, "getReadableDatabase called");
		Log.d(TAG, "dbNeedsRefreshing: " + dbNeedsRefreshing);
		
		if (dbNeedsRefreshing) {
			refreshDb();
		}
		
		return mDatabase;
	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		// this will create a database if one doesn't already exist
		mDatabase = super.getWritableDatabase();
		
		Log.d(TAG, "getWritableDatabase called");
		Log.d(TAG, "dbNeedsRefreshing: " + dbNeedsRefreshing);
		
		if (dbNeedsRefreshing) {
			refreshDb();
		}

		return mDatabase;
	}
	
	/**
	 * Handles everything that's necessary to do before and after copying the 
	 * packaged database to the application database on the system.
	 */
	private void refreshDb() {
		try {
			Log.d(TAG, "db version: " + mDatabase.getVersion());
			// keep track of whether the database was read only
			boolean readOnly = mDatabase.isReadOnly();
			/*  the database will be open at this point (after a 
			 *  getReadable/Writable database call) and needs to be closed
			 *  so it can be overwritten with our packaged db
			 */
			mDatabase.close();
			copyDatabase();
			// reopen database connection as writable, which should update the version
			mDatabase = super.getWritableDatabase();
			// update the version manually in case it gets missed somewhere
			if (mDatabase.getVersion() != DATABASE_VERSION) {
				Log.d(TAG, "changing version of db to " + DATABASE_VERSION);
				mDatabase.setVersion(DATABASE_VERSION);
			}
			// if the database was previously read only, close and reopen read only
			if (readOnly) {
				mDatabase.close();
				mDatabase = super.getReadableDatabase();
			}
		} catch (IOException e) {
// TODO: how do we properly handle errors here?
			e.printStackTrace();
		}
		dbNeedsRefreshing = false;
	}
	
    /**
     * Copies the packaged database from the local assets folder to the existing 
     * database in the system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDatabase() throws IOException {
    	// Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open("databases/" + DATABASE_NAME);
    	
    	// Path to the just created empty db
    	String outFileName = myContext.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    	
    	// Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	
    	// transfer bytes from the input file to the output file
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer)) > 0) {
    		myOutput.write(buffer, 0, length);
    	}
    	
    	// Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		dbNeedsRefreshing = true;
		Log.d(TAG, "onCreate called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dbNeedsRefreshing = true;
		Log.d(TAG, "onUpgrade called");
		
		// in version 12 we renamed the db file; delete the old one
		if (oldVersion < 12) {
		    File oldDb = new File(myContext.getDatabasePath("words.db").getAbsolutePath());
		    oldDb.delete();
		}
	}
}
