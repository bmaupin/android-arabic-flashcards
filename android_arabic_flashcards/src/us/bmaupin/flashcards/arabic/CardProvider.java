package us.bmaupin.flashcards.arabic;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class CardProvider extends ContentProvider {
    public static final class Cards implements BaseColumns {
        public static final String AUTHORITY = "us.bmaupin.flashcards.arabic.cardprovider";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    }
    
    private static final int CARDS = 1;
    private static final String CARDS_BASE_PATH = "cards";
    private static final int CARD_ID = 2;
    
    private CardDatabaseHelper cardDbHelper;
    private static HashMap<String, String> sCardsProjectionMap;


/*
	private static final String AUTHORITY = "com.mamlambo.tutorial.tutlist.data.TutListProvider";
	public static final int TUTORIALS = 100;
	public static final int TUTORIAL_ID = 110;
	 
	private static final String TUTORIALS_BASE_PATH = "tutorials";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
	        + "/" + TUTORIALS_BASE_PATH);
	 
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
	        + "/mt-tutorial";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
	        + "/mt-tutorial";
*/
	
    private static final UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Cards.AUTHORITY, CARDS_BASE_PATH, CARDS);
        sUriMatcher.addURI(Cards.AUTHORITY, CARDS_BASE_PATH + "/#", CARD_ID);
        
        sCardsProjectionMap = new HashMap<String, String>();
        sCardsProjectionMap = new HashMap<String, String>();

    }
    
    @Override
    public boolean onCreate() {
        cardDbHelper = new CardDatabaseHelper(getContext());
        
        return true;
    }
    
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sort) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CardDatabaseHelper.CARDS_TABLE);
        
        switch (sUriMatcher.match(uri)) {
            case CARDS:
                qb.setProjectionMap(sNotesProjectionMap);
                break;
        }

        return null;
    }
	
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
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


}
