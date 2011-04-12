package us.bmaupin.flashcards.arabic;

// $Id$

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class ProfileDatabaseHelper extends SQLiteOpenHelper {
	// The tag will be used for logging
    public static final String TAG = "MyDatabase";

    // The name of your database
    public static final String DATABASE_NAME = "mydatabase.db";
    // The version of your database (increment this every time you change something)
    public static final int DATABASE_VERSION = 1;
    // The name of the table in your database
    public static final String DB_TABLE_NAME = "mytable";
   
    // The name of each column in the database
    public static final String CARD_ID = "title";
    public static final String STATUS = "description";
    public static final String TAGS = "tags";

    // SQL Statement to create a new database.
    public static final String DB_TABLE_CREATE =
        "CREATE TABLE " + DB_TABLE_NAME + " (" +
        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CARD_ID + " INTEGER, " +
        STATUS + " INTEGER, " +
        TAGS + " TEXT);";
   
    // The constructor method
    public ProfileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public String getProfileName() {
    	return "mytable";
    }
    
    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the database doesn't exist yet and needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_TABLE_CREATE);       
    }

    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the existing database isn't the same version as
     * the latest version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
        onCreate(db);
    }
}