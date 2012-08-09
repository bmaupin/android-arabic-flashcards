package ca.bmaupin.flashcards.arabic.data;

import org.amr.arabic.ArabicUtilities;

import ca.bmaupin.flashcards.arabic.Cards;
import ca.bmaupin.flashcards.arabic.R;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

public class CardProvider extends ContentProvider {
    private static final String TAG = "CardProvider";
    // various constants
    public static final String AUTHORITY = 
            "ca.bmaupin.flashcards.arabic.cardprovider";
    private static final int CARD_ID_PATH_POSITION = 1;
    private static final String PATH_CARDS = "cards";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY 
            + "/" + PATH_CARDS);
    
    // content provider data type constants
    private static final int CARDS = 1;
    private static final int CARD_ID = 2;
    private static final int SEARCH_SUGGEST = 3;
    
    // content provider mime type constants
    public static final String CONTENT_TYPE = 
            "vnd.android.cursor.dir/vnd.bmaupin.card";
    public static final String CONTENT_ITEM_TYPE = 
            "vnd.android.cursor.item/vnd.bmaupin.card";
    
    // query parameter for whether or not to do a join
    public static final String QUERY_PARAMETER_JOIN = "join";
    // query parameter for limiting the results of the query
    public static final String QUERY_PARAMETER_LIMIT = "limit";
    public static final String QUERY_PARAMETER_VALUE_FALSE = "0";
    
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
        sUriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
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
        
        String arabicColumn = "";
        String limit = null;
        
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CardDatabaseHelper.CARDS_TABLE);
        
        switch (sUriMatcher.match(uri)) {
        case CARDS:
            limit = uri.getQueryParameter(QUERY_PARAMETER_LIMIT);
            // if we're searching for cards by ahlan wa sahlan chapter
            if (selection.indexOf(CardDatabaseHelper.AWS_CHAPTERS_CHAPTER)
                    != -1) {
                String join = uri.getQueryParameter(QUERY_PARAMETER_JOIN);
                // only do the join if we haven't specified in a query parameter not to
                if (join == null || !join.equals(QUERY_PARAMETER_VALUE_FALSE)) {
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
            }
            break;
        
        case CARD_ID:
            qb.appendWhere(CardDatabaseHelper._ID + "=" + 
                    uri.getPathSegments().get(CARD_ID_PATH_POSITION));
            break;
        
        case SEARCH_SUGGEST:
            // get the query and trim whitespace
            String query = uri.getLastPathSegment().toLowerCase().trim();
            
            // if the first character of the query is arabic
            if (ArabicUtilities.isArabicCharacter(query.charAt(0))) {
                String arabicQuery = "%";
                // insert % between each character to account for vowels
                for (int i=0; i < query.length(); i++) {
                    arabicQuery += query.charAt(i) + "%";
                }
                
                selection = CardDatabaseHelper.CARDS_ARABIC + " LIKE ?";
                selectionArgs = new String[] {arabicQuery};
                
                projection = new String[] {
                        CardDatabaseHelper._ID,
                        CardDatabaseHelper.CARDS_ARABIC + " AS " + 
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                        CardDatabaseHelper.CARDS_ENGLISH + " AS " + 
                                SearchManager.SUGGEST_COLUMN_TEXT_2,
                        CardDatabaseHelper._ID + " AS " + 
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA
                };
                
                arabicColumn = SearchManager.SUGGEST_COLUMN_TEXT_1;
            
            // if the query is for an english word
            } else {
                // append percent signs for partial matches
                query = "%" + query + "%";
                selection = CardDatabaseHelper.CARDS_ENGLISH + " LIKE ?";
                selectionArgs = new String[] {query};
                
                projection = new String[] {
                        CardDatabaseHelper._ID,
                        CardDatabaseHelper.CARDS_ENGLISH + " AS " + 
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                        CardDatabaseHelper.CARDS_ARABIC + " AS " + 
                                SearchManager.SUGGEST_COLUMN_TEXT_2,
                        CardDatabaseHelper._ID + " AS " + 
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA
                };
                
                arabicColumn = SearchManager.SUGGEST_COLUMN_TEXT_2;
            }
            
        	// for android 2.1 and below, if the fix arabic preference is true
            if (Integer.parseInt(Build.VERSION.SDK) <= 7 &&
            		PreferenceManager.getDefaultSharedPreferences(getContext()).
            		getBoolean(getContext().getString(
            		R.string.preferences_fix_arabic), getContext().
            		getResources().getBoolean(
            		R.bool.preferences_fix_arabic_default))) {
        		// hide arabic from the search suggestions. it just shows
        		// up as rectangular boxes anyway
                projection = new String[] {
                        CardDatabaseHelper._ID,
                        CardDatabaseHelper.CARDS_ENGLISH + " AS " + 
                                SearchManager.SUGGEST_COLUMN_TEXT_1,
                        CardDatabaseHelper._ID + " AS " + 
                                SearchManager.SUGGEST_COLUMN_INTENT_DATA
                };
            }
            
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
                limit           // limit 
            );
        
        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
        if (sUriMatcher.match(uri) == SEARCH_SUGGEST) {
        	// for android 2.2 - 2.3, if the fix arabic preference is true
        	if (Integer.parseInt(Build.VERSION.SDK) >= 8 &&
        			Integer.parseInt(Build.VERSION.SDK) <= 10 &&
        			PreferenceManager.getDefaultSharedPreferences(
        			getContext()).getBoolean(getContext().getString(
            		R.string.preferences_fix_arabic), getContext().
            		getResources().getBoolean(
            		R.bool.preferences_fix_arabic_default))) {
        		// return a custom cursor wrapper that reshapes the arabic
        	    // and removes the vowels
        		return new MyCursorWrapper(c, arabicColumn, true);
        		
        	// for android 3 - 4.0, if the fix arabic preference is true
        	} else if (Integer.parseInt(Build.VERSION.SDK) >= 11 &&
                    Integer.parseInt(Build.VERSION.SDK) <= 15 &&
                    PreferenceManager.getDefaultSharedPreferences(
                    getContext()).getBoolean(getContext().getString(
                    R.string.preferences_fix_arabic), getContext().
                    getResources().getBoolean(
                    R.bool.preferences_fix_arabic_default))) {
                // return a custom cursor wrapper that removes the vowels
                return new MyCursorWrapper(c, arabicColumn, false);
            }
        }
        
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
    
    private class MyCursorWrapper extends CursorWrapper {
    	private String arabicColumn;
    	private boolean reshapeArabic;
    	
        public MyCursorWrapper(Cursor cursor, String arabicColumn, 
                boolean reshapeArabic) {
            super(cursor);
            
            this.arabicColumn = arabicColumn;
            this.reshapeArabic = reshapeArabic;
        }

        @Override
        public String getString(int columnIndex) {
        	if (getColumnName(columnIndex).equals(arabicColumn)) {
        	    if (reshapeArabic) {
        	        return Cards.fixArabic(Cards.removeVowels(super.getString(
        	                columnIndex)), false);
        	    } else {
        	        return Cards.removeVowels(super.getString(columnIndex));
        	    }
            } else {
                return super.getString(columnIndex);
            }
        }
    }
}