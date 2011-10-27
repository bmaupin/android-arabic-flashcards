package us.bmaupin.flashcards.arabic;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class CardProvider extends ContentProvider {
    // various constants
    public static final String AUTHORITY = "us.bmaupin.flashcards.arabic.cardprovider";
    private static final int CARD_ID_PATH_POSITION = 1;
    private static final String PATH_CARDS = "cards";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY 
            + "/" + PATH_CARDS);
    private static final String TAG = "CardProvider";
    // content provider data type constants
    private static final int CARDS = 1;
    private static final int CARD_ID = 2;
    // content provider mime type constants
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.bmaupin.card";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.bmaupin.card";
    
    private CardDatabaseHelper cardDbHelper;

    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, PATH_CARDS, CARDS);
        sUriMatcher.addURI(AUTHORITY, PATH_CARDS + "/#", CARD_ID);
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
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CardDatabaseHelper.CARDS_TABLE);
        
        switch (sUriMatcher.match(uri)) {
            case CARDS:
                break;
            
            case CARD_ID:
                qb.appendWhere(CardDatabaseHelper._ID + "=" + 
                        uri.getPathSegments().get(CARD_ID_PATH_POSITION));
                break;
                
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

/*
        String orderBy;
        // If no sort order is specified, uses the default
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = NotePad.Notes.DEFAULT_SORT_ORDER;
        } else {
            // otherwise, uses the incoming sort order
            orderBy = sortOrder;
        }
*/

        SQLiteDatabase db = cardDbHelper.getReadableDatabase();

        Cursor c = qb.query(
                db,             // The database to query
                projection,     // The columns to return from the query
                selection,      // The columns for the where clause
                selectionArgs,  // The values for the where clause
                null,          // don't group the rows
                null,          // don't filter by row groups
                sortOrder       // The sort order
            );

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
	
    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        // TODO Auto-generated method stub
        return 0;
    }
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
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
