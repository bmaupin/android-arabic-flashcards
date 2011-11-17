package us.bmaupin.flashcards.arabic;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class StudySetDatabaseHelper extends SQLiteOpenHelper {
    // The tag will be used for logging
    public static final String TAG = "StudySetDatabaseHelper";
    
    // The name of your database
    public static final String DATABASE_NAME = "studysets.db";
    // The version of your database (increment this every time you change something)
    public static final int DATABASE_VERSION = 1;
    // The name of the table in your database
    public static final String META_TABLE_NAME = "studysets_meta";
    
    // The name of each column in the database
    public static final String _ID = BaseColumns._ID;
//    public static final String SET_ID = "set_id";
    public static final String META_SET_NAME = "set_name";
    public static final String META_SET_LANGUAGE = "set_language";
    
    public static final String SET_CARD_ID = "card_id";
    public static final String SET_INTERVAL = "interval";
    public static final String SET_DUE_TIME = "due_time";
    
    // SQL Statement to create a new database.
    public static final String META_TABLE_CREATE =
        "CREATE TABLE " + META_TABLE_NAME + " (" +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//        SET_ID + " TEXT, " +
        META_SET_NAME + " TEXT, " +
        META_SET_LANGUAGE + ");";
    
    // The constructor method
    public StudySetDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the database doesn't exist yet and needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(META_TABLE_CREATE);       
    }

    /* Called when the super class getWritableDatabase (or getReadableDatabase)
     * method determines that the existing database isn't the same version as
     * the latest version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        // this is probably not the best way to do it...
//        db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE_NAME);
//        onCreate(db);
    }
}