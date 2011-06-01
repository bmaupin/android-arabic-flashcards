package us.bmaupin.flashcards.arabic;

//$Id$

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
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
    private String profileTableName = "profile1";
    
    // The name of each column in the database
    public static final String CARD_ID = "card_ID";
    public static final String STATUS = "status";
    
    // SQL Statement to create a new database.
    private final String DB_TABLE_CREATE =
        "CREATE TABLE %s (" +
        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CARD_ID + " INTEGER UNIQUE, " +
        STATUS + " INTEGER);";

    // The constructor method
    public ProfileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
// TODO: maybe we should just create a separate method: verifyTableExists?    
    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        List<String> tables = new ArrayList<String>();
        final String SQL_GET_ALL_TABLES = "SELECT name FROM " + 
            "sqlite_master WHERE type='table' ORDER BY name";
        
        // fetch the database
        SQLiteDatabase db = super.getReadableDatabase();
        // get the list of tables in the db
        Cursor cursor = db.rawQuery(SQL_GET_ALL_TABLES, null);
        while (cursor.moveToNext()) {
            tables.add(cursor.getString(0));
        }
        cursor.close();
        // if the table we want isn't in the db, create it
        if (!tables.contains(profileTableName)) {
            db.execSQL(String.format(DB_TABLE_CREATE, profileTableName));
        }
        
        return db;
    }
    
    public synchronized SQLiteDatabase getReadableDatabase(String profileTableName) {
        this.profileTableName = profileTableName;
        return getReadableDatabase();
    }
    
    public String getprofileTableName() {
        return profileTableName;
    }
    
    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the database doesn't exist yet and needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
//
        Log.d(TAG, "onCreate called");
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// nothing to do here
	}
}
