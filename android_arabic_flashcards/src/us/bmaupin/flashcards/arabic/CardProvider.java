package us.bmaupin.flashcards.arabic;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class CardProvider extends ContentProvider {
	private static final String AUTHORITY = "us.bmaupin.flashcards.arabic.cardprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

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
	
	private CardDatabaseHelper cardDbHelper;
	
	
	
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
	public boolean onCreate() {
		cardDbHelper = new CardDatabaseHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}

}
