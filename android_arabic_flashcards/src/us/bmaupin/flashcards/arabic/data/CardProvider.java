package us.bmaupin.flashcards.arabic.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class CardProvider extends ContentProvider {
	private static final String TAG = "CardProvider";
    // various constants
    public static final String AUTHORITY = 
    		"us.bmaupin.flashcards.arabic.cardprovider";
    private static final int CARD_ID_PATH_POSITION = 1;
    private static final int LIMIT_PATH_POSITION = 2;
    private static final String PATH_CARDS = "cards";
    private static final String PATH_LIMIT = "limit";
    public static final Uri CONTENT_URI_CARDS = Uri.parse("content://" + 
    		AUTHORITY + "/" + PATH_CARDS);
    public static final Uri CONTENT_URI_CARDS_LIMIT = Uri.parse("content://" + 
    		AUTHORITY + "/" + PATH_CARDS + "/" + PATH_LIMIT);
    // content provider data type constants
    private static final int CARDS = 1;
    private static final int CARD_ID = 2;
    private static final int CARDS_LIMIT = 3;
    // content provider mime type constants
    public static final String CONTENT_TYPE = 
    		"vnd.android.cursor.dir/vnd.bmaupin.card";
    public static final String CONTENT_ITEM_TYPE = 
    		"vnd.android.cursor.item/vnd.bmaupin.card";
    
    private CardDatabaseHelper cardDbHelper;

    private static final UriMatcher sUriMatcher;

    /* this part just defines which URI formats are valid.  it doesn't actually
     * do any URI parsing.  all the parsing is done in the classes below which 
     * actually take a content URI.
     */
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_CARDS, CARDS);
        sUriMatcher.addURI(AUTHORITY, PATH_CARDS + "/#", CARD_ID);
        sUriMatcher.addURI(AUTHORITY, PATH_CARDS + "/" + PATH_LIMIT + "/#", 
        		CARDS_LIMIT);
    }
    
    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate()");
        cardDbHelper = new CardDatabaseHelper(getContext());
        
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query()");
        
        String limit = null;
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CardDatabaseHelper.CARDS_TABLE);
        
        switch (sUriMatcher.match(uri)) {
        case CARDS_LIMIT:
        	limit = uri.getPathSegments().get(LIMIT_PATH_POSITION);
        
        case CARDS:
        	// if we're searching for cards by ahlan wa sahlan chapter
        	if (selection.indexOf(CardDatabaseHelper.AWS_CHAPTERS_CHAPTER)
        			!= -1) {
        		/*
        		 * this looks like:
        		 * cards left join aws_chapters on cards._id = aws_chapters.card_id
        		 */
        		qb.setTables(CardDatabaseHelper.CARDS_TABLE + " LEFT " +
        				"JOIN " + CardDatabaseHelper.AWS_CHAPTERS_TABLE + 
        				" ON " + CardDatabaseHelper.CARDS_TABLE + "." + 
        				CardDatabaseHelper._ID + " = " + 
        				CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." + 
        				CardDatabaseHelper.AWS_CHAPTERS_CARD_ID);
        	}
            break;
        
        case CARD_ID:
            qb.appendWhere(CardDatabaseHelper._ID + "=" + 
                    uri.getPathSegments().get(CARD_ID_PATH_POSITION));
            break;
            

            
        default:
            // If the URI doesn't match any of the known patterns, throw an exception.
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = cardDbHelper.getReadableDatabase();

        Cursor c = qb.query(
                db,             // The database to query
                projection,     // The columns to return from the query
                selection,      // The columns for the where clause
                selectionArgs,  // The values for the where clause
                null,           // don't group the rows
                null,           // don't filter by row groups
                sortOrder,      // The sort order
                limit          // limit 
            );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
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
       switch (sUriMatcher.match(uri)) {

           case CARDS:
               return CONTENT_TYPE;

           case CARD_ID:
               return CONTENT_ITEM_TYPE;

           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }
    }
}
