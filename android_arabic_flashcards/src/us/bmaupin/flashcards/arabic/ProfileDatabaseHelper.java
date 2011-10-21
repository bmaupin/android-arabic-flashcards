package us.bmaupin.flashcards.arabic;

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
    private static final String TAG = "ProfileDatabaseHelper";

    // The name of your database
    public static final String DATABASE_NAME = "profiles.db";
    // The version of your database (increment this every time you change something)
    public static final int DATABASE_VERSION = 1;
    // profile name; this will be used as the database table name
    private static final String DEFAULT_PROFILE_TABLE_NAME = "profile1";
    private String profileTableName = "";
    
    // The name of each column in the database
    public static final String _ID = BaseColumns._ID;
    public static final String CARD_ID = "card_ID";
    public static final String STATUS = "status";
    
    // SQL Statement to create a new database.
    private final String DB_TABLE_CREATE =
        "CREATE TABLE %s (" +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CARD_ID + " INTEGER UNIQUE, " +
        STATUS + " INTEGER);";

    // The constructor method
    public ProfileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    void setProfileTableName(String profileTableName) {
        if (profileTableName.equals("")) {
            this.profileTableName = DEFAULT_PROFILE_TABLE_NAME;
        } else {
            this.profileTableName = profileTableName;
        }
        verifyProfileTableExists(this.profileTableName);
    }
    
    public String getProfileTableName() {
        return profileTableName;
    }
    
    private void verifyProfileTableExists(String profileTableName) {
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
        
        db.close();
    }
    
    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the database doesn't exist yet and needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // don't do anything here because we'll create the profile tables manually
//
        Log.d(TAG, "onCreate called");
    }

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// nothing to do here
	}
}
