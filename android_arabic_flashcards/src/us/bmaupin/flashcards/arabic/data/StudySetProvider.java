package us.bmaupin.flashcards.arabic.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class StudySetProvider extends ContentProvider {
	private static final String TAG = "StudySetProvider";
	
    public static final String AUTHORITY = 
        "us.bmaupin.flashcards.arabic.studysetprovider";
    // uri path constants
    private static final String PATH_STUDYSETS = "studysets";
    private static final String PATH_CARDS = "cards";
    private static final String PATH_META = "meta";
    // content uris
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + 
    		"/" + PATH_STUDYSETS);
    public static final Uri CONTENT_URI_META = Uri.parse("content://" + AUTHORITY + 
    		"/" + PATH_STUDYSETS + "/" + PATH_META);
    // uri pattern constants
    private static final int STUDYSETS = 1;
    private static final int STUDYSETS_ID = 2;
    private static final int STUDYSETS_CARDS_ID = 3;
    private static final int STUDYSETS_META = 4;
    private static final int STUDYSETS_META_ID = 5;
    
    private static final UriMatcher sUriMatcher;

    /* this part just defines which URI formats are valid.  it doesn't actually
     * do any URI parsing.  all the parsing is done in the classes below which 
     * actually take a content URI.
     */
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_STUDYSETS, STUDYSETS);
        sUriMatcher.addURI(AUTHORITY, PATH_STUDYSETS + "/#", STUDYSETS_ID);
        sUriMatcher.addURI(AUTHORITY, PATH_STUDYSETS + "/#" + PATH_CARDS + "/#", 
        		STUDYSETS_CARDS_ID);
        sUriMatcher.addURI(AUTHORITY, PATH_STUDYSETS + "/" + PATH_META, 
        		STUDYSETS_META);
        sUriMatcher.addURI(AUTHORITY, PATH_STUDYSETS + "/" + PATH_META + "/#", 
        		STUDYSETS_META_ID);
    }
    
    private StudySetDatabaseHelper dbHelper;
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        dbHelper = new StudySetDatabaseHelper(getContext());
        
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
    	Log.d(TAG, "query()");
    	
    	String limit = null;
    	
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	
    	switch (sUriMatcher.match(uri)) {
    	case STUDYSETS:
    		break;
    		
    	case STUDYSETS_ID:
    		break;
    		
    	case STUDYSETS_CARDS_ID:
    		break;
		
    	case STUDYSETS_META:
    	    qb.setTables(StudySetDatabaseHelper.META_TABLE_NAME);
    	    
    		break;
    	
    	case STUDYSETS_META_ID:
    		break;
    	
    	default:
            // If the URI doesn't match any of the known patterns, throw an exception.
            throw new IllegalArgumentException("Unknown URI " + uri);
    	}
    	
    	SQLiteDatabase db = dbHelper.getReadableDatabase();
    	
        Cursor c = qb.query(
                db,             // The database to query
                projection,     // The columns to return from the query
                selection,      // The columns for the where clause
                selectionArgs,  // The values for the where clause
                null,           // don't group the rows
                null,           // don't filter by row groups
                sortOrder,      // The sort order
                limit           // limit 
            );
    	
        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
	
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            
        case STUDYSETS_ID:
            break;
        
        case STUDYSETS_META:
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long newStudySetId = db.insert(
                    StudySetDatabaseHelper.META_TABLE_NAME, 
                    StudySetDatabaseHelper._ID, values);
            // if the insertion of the new study set into meta was successful
            if (newStudySetId > 0) {
                // create the new study set table
                dbHelper.createNewStudySet(db, newStudySetId);
                
                Uri studySetUri = ContentUris.withAppendedId(CONTENT_URI_META, 
                        newStudySetId);
                getContext().getContentResolver().notifyChange(studySetUri, 
                        null);
                return studySetUri;
            }
            
            throw new SQLException("Failed to insert row into " + uri);
        
        default:
            // If the URI doesn't match any of the known patterns, throw an exception.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        
        return null;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String selection, 
    		String[] selectionArgs) {
        // not going to implement
        return 0;
    }
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
        // not going to implement
		return 0;
	}

	@Override
    public String getType(Uri uri) {
		// not going to implement
        return null;
    }
}
