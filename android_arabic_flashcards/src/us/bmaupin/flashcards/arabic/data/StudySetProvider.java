package us.bmaupin.flashcards.arabic.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
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
    	
    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    	
    	return null;
    }
	
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // not going to implement
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
