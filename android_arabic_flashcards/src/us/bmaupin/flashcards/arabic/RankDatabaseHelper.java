package us.bmaupin.flashcards.arabic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class RankDatabaseHelper extends SQLiteOpenHelper {
	// tag for log messages
	public static final String TAG = "RankDatabaseHelper";

    // The name of your database
    public static final String DATABASE_NAME = "ranks.db";
    // The version of your database (increment this every time you change something)
    public static final int DATABASE_VERSION = 1;
    // The name of the table in your database
    public static final String DB_TABLE_NAME = "ranks";
   
    // The name of each column in the database
    public static final String KEY_RANK = "rank";

    // SQL Statement to create a new database.
    public static final String DB_TABLE_CREATE =
        "CREATE TABLE " + DB_TABLE_NAME + " (" +
        BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        KEY_RANK + " INTEGER);";
   
    // The constructor method
    public RankDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    
    void initializeDb (SQLiteDatabase db, int totalRows) {
    	String sql = "SELECT COALESCE(MAX(_ID)+1, 0) FROM " + DB_TABLE_NAME;
    	Cursor cursor = db.rawQuery(sql, null);
    	cursor.moveToFirst();
    	int existingRows = cursor.getInt(0);
    	Log.d(TAG, "initializeDb: existingRows=" + existingRows );
    	cursor.close();
    	
    	// TODO: fill db with data up to number of total rows
    }
}
