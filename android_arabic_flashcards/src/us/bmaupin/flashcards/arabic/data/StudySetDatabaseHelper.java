package us.bmaupin.flashcards.arabic.data;

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
    public static final int DATABASE_VERSION = 4;
    // The name of the table in your database
    public static final String META_TABLE_NAME = "studysets_meta";
    public static final String SET_TABLE_PREFIX = "set_";
    
    // The name of each column in the database
    public static final String _ID = BaseColumns._ID;
    public static final String META_SET_NAME = "set_name";
    public static final String META_CARD_GROUP = "card_group";
    public static final String META_CARD_SUBGROUP = "card_subgroup";
    public static final String META_SET_LANGUAGE = "set_language";
    // last date new cards were shown
    public static final String META_NEW_CARDS_DATE = "new_cards_date";
    // how many cards were shown on META_NEW_CARDS_DATE
    public static final String META_NEW_CARDS_SHOWN = "new_cards_shown";
    
    public static final String SET_CARD_ID = "card_id";
    // card interval (in hours)
    public static final String SET_INTERVAL = "interval";
    // timestamp card is due
    public static final String SET_DUE_TIME = "due_time";
    
    public static final String COUNT = "COUNT()";
    // date format of META_NEW_CARDS_DATE column
    public static final String META_NEW_CARDS_DATE_FORMAT = "yyyyMMdd";
    
    // SQL Statement to create a new database table
    public static final String META_TABLE_CREATE =
        "CREATE TABLE " + META_TABLE_NAME + " (" +
        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        META_SET_NAME + " TEXT, " +
        META_CARD_GROUP + " TEXT, " +
        META_CARD_SUBGROUP + " TEXT, " +
        META_SET_LANGUAGE + " TEXT, " +
        META_NEW_CARDS_DATE + " TEXT NOT NULL, " +
        META_NEW_CARDS_SHOWN + " INTEGER);";
    
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
                + newVersion);
        // this is probably not the best way to do it...
        db.execSQL("DROP TABLE IF EXISTS " + META_TABLE_NAME);
        onCreate(db);
    }
    
    public void createNewStudySet(SQLiteDatabase db, long studySetId) {
        final String STUDY_SET_TABLE_CREATE =
            "CREATE TABLE " + SET_TABLE_PREFIX + studySetId + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            SET_CARD_ID + " INTEGER UNIQUE, " +
            SET_INTERVAL + " INTEGER, " +
            SET_DUE_TIME + " INTEGER);";
        
        db.execSQL(STUDY_SET_TABLE_CREATE);
    }
    
    public void deleteStudySet(SQLiteDatabase db, String studySetId) {
        final String STUDY_SET_TABLE_DELETE = 
            "DROP TABLE IF EXISTS " + SET_TABLE_PREFIX + studySetId;
        
        db.execSQL(STUDY_SET_TABLE_DELETE);
    }
}
