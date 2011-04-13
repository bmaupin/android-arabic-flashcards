package us.bmaupin.flashcards.arabic;

// $Id$

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class ProfileDatabaseHelper extends SQLiteOpenHelper {
	// tag for log messages
	public static final String TAG = "ProfileDatabaseHelper";

    // The name of your database
    public static final String DATABASE_NAME = "profiles.db";
    // The version of your database (increment this every time you change something)
    public static final int DATABASE_VERSION = 1;
    // profile name; this will be used as the database table name
    private String profileName;
    private static final String DEFAULT_PROFILE_NAME = "profile1";
    
    // The name of each column in the database
    public static final String CARD_ID = "card_ID";
    public static final String STATUS = "status";
    
    // SQL Statement to create a new database.
    private String DB_TABLE_CREATE =
        "CREATE TABLE " + profileName + " (" +
        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CARD_ID + " INTEGER, " +
        STATUS + " INTEGER);";
    
    // The constructor method
    public ProfileDatabaseHelper(Context context, String profileName) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.profileName = profileName;
    }
    
    public ProfileDatabaseHelper(Context context) {
    	this(context, DEFAULT_PROFILE_NAME);
    }
    
    public String getProfileName() {
    	return profileName;
    }
    
    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the database doesn't exist yet and needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d(TAG, "onCreate called");
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
//        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
//        onCreate(db);
// TODO: upgrade the db properly
    }
}
