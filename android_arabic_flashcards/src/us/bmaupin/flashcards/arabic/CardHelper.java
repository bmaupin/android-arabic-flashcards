package us.bmaupin.flashcards.arabic;

// $Id$

/*
 * TODO
 * - pop up prompt the first time we see a known card per cursor
 * - pop up prompt when we get to the end of the stack of cards
 * 
 */

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class CardHelper {
    private static final String PROFILE_DB = "profileDb";
    private static final String TAG = "CardHelper";
    
    private String currentCategory = "All";
    private String currentSubCategory = "";
    private Cursor cursor = null;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String profileName = "";
    
    public CardHelper(Context context) {
    	this(context, "");
    }
    
    public CardHelper(Context context, String profileName) {
    	ProfileDatabaseHelper profileHelper = new ProfileDatabaseHelper(context);
    	
    	// set the profile table name, which will ensure the profile table exists
    	profileHelper.setProfileTableName(profileName);

    	if (profileName.equals("")) {
    	    this.profileName = profileHelper.getProfileTableName();
    	} else {
    	    this.profileName = profileName;
    	}

    	profileHelper.close();
        
        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getReadableDatabase();
        db.execSQL("attach database ? as " + PROFILE_DB, 
            new String[] {context.getDatabasePath(ProfileDatabaseHelper.DATABASE_NAME).getPath()});
    }
    
    public void close() {
        // clean up after ourselves
        cursor.close();
        db.close();
        dbHelper.close();
    }
    
    void loadCategory(String category) {
        currentCategory = category;
        // close the cursor so we'll reload it with the new category
        cursor.close();
    }
    
    void loadCategory(String category, String subCategory) {
        currentCategory = category;
        currentSubCategory = subCategory;
        // close the cursor so we'll reload it with the new category
        cursor.close();
    }
    
    void startOver() {
        // close the cursor so we create a new one to start over
        cursor.close();
    }

    void loadCardsCursor() {
        String sqlCategorySelection = "";
        
        if (currentCategory.equals("Ahlan wa sahlan")) {
            sqlCategorySelection = "WHERE aws_chapter = " + currentSubCategory;
        }
// TODO: finish implementing card category selection
    	
        String sql = "SELECT " + DatabaseHelper.DB_TABLE_NAME + "." + 
        DatabaseHelper._ID + ", " +
        DatabaseHelper.ENGLISH + ", " +
        DatabaseHelper.ARABIC + ", " +
        ProfileDatabaseHelper.STATUS +
        " FROM " + DatabaseHelper.DB_TABLE_NAME +
        " LEFT JOIN profileDb." + profileName + 
        " ON " + DatabaseHelper.DB_TABLE_NAME + "." + BaseColumns._ID
        + " = profileDb." + 
        profileName + "." + 
        ProfileDatabaseHelper.CARD_ID + " %s ORDER BY " + 
        ProfileDatabaseHelper.STATUS;
        
        Log.d(TAG, "rawQuery=" + String.format(sql, sqlCategorySelection));
        
        cursor = db.rawQuery(String.format(sql, sqlCategorySelection), null);
    }
    
    Map<String, String> nextCard() {
//
    	Log.d(TAG, "nextCard called");

    	// the cursor will be null if we haven't created one yet
    	// it will be closed if we're changing categories or starting over
    	if (cursor == null || cursor.isClosed()) {
    	    loadCardsCursor();
    	}
    	
    	if (cursor.isLast()) {
    	    // return a blank card so we can show the user a message that
            // there aren't any more cards
    	    return new HashMap<String, String>();
    	} else {
    	    cursor.moveToNext();
    	}

// TODO: somewhere in here, prompt user once we've seen all but the known cards
//      NOTE: only do this once per cursor.  if we do it more than once, it might trigger when going back through history
        
        return getCurrentCard();
    }
    
    Map<String, String> prevCard() {
        if (cursor.isFirst()) {
            // return a blank card so we can show the user a message that there
            // aren't any previous cards
            return new HashMap<String, String>();
        } else {
            cursor.moveToPrevious();
        }

        return getCurrentCard();
    }
    
    private Map<String, String> getCurrentCard() {
        Map<String, String> thisCard = new HashMap<String, String>();
        
        // if arabic is empty and english is an integer, it's a reference to 
        // another card
        if (cursor.getString(2).equals("") && ArabicFlashcards.stringToInteger(cursor.getString(1)) != 0) {
            // so return the other card
            return getCardById(ArabicFlashcards.stringToInteger(cursor.getString(1)));
        }
        
        thisCard.put("ID", cursor.getString(0));
        thisCard.put("english", cursor.getString(1));
        thisCard.put("arabic", cursor.getString(2));
        // card status might be null if the card hasn't been seen yet
        if (cursor.getString(3) == null) {
        	thisCard.put("status", "0");
        } else {
        	thisCard.put("status", cursor.getString(3));
        }
//        
        Log.d(TAG, "getCurrentCard: _id=" + cursor.getString(0));
        Log.d(TAG, "getCurrentCard: english=" + cursor.getString(1));
        Log.d(TAG, "getCurrentCard: arabic=" + cursor.getString(2));
        Log.d(TAG, "getCurrentCard: status=" + cursor.getString(3));
        
        return thisCard;
    }

    private Map<String, String> getCardById(int thisId) {
        Log.d(TAG, "getCard: thisId=" + thisId);
        Map<String, String> thisCard = new HashMap<String, String>();
        
        final String sql = "SELECT " +
            DatabaseHelper.ENGLISH + ", " +
            DatabaseHelper.ARABIC + ", " +
            ProfileDatabaseHelper.STATUS +
            " FROM " + DatabaseHelper.DB_TABLE_NAME +
            " LEFT JOIN " + PROFILE_DB + "." + profileName +
            " ON " + DatabaseHelper.DB_TABLE_NAME + "." + BaseColumns._ID
            + " = profileDb." + 
            profileName + "." + 
            ProfileDatabaseHelper.CARD_ID + 
            " WHERE " + DatabaseHelper.DB_TABLE_NAME + "." +
            DatabaseHelper._ID + " = %s";
        
        Cursor thisCursor = db.rawQuery(String.format(sql, thisId), null);
        thisCursor.moveToFirst();
        
        thisCard.put("ID", "" + thisId);
        thisCard.put("english", thisCursor.getString(0));
        thisCard.put("arabic", thisCursor.getString(1));
        // card status might be null if the card hasn't been seen yet
        if (thisCursor.getString(2) == null) {
            thisCard.put("status", "0");
        } else {
            thisCard.put("status", thisCursor.getString(2));
        }
//        
        Log.d(TAG, "getCard: english=" + thisCursor.getString(0));
        Log.d(TAG, "getCard: arabic=" + thisCursor.getString(1));
        Log.d(TAG, "getCard: status=" + thisCursor.getString(2));
        
        thisCursor.close();
        
        return thisCard;
    }
    
/*
    private Map<String, String> getCardById(int thisId, boolean addToHistory) {
        Log.d(TAG, "getCard: thisId=" + thisId);
        Map<String, String> thisCard = new HashMap<String, String>();
        
        final String sql = "SELECT " +
        	DatabaseHelper.ENGLISH + ", " +
	        DatabaseHelper.ARABIC + ", " +
	        ProfileDatabaseHelper.STATUS +
	        " FROM " + DatabaseHelper.DB_TABLE_NAME +
	        " LEFT JOIN " + PROFILE_DB + "." + profileName +
	        " ON " + DatabaseHelper.DB_TABLE_NAME + "." + BaseColumns._ID
	        + " = profileDb." + 
	        profileName + "." + 
	        ProfileDatabaseHelper.CARD_ID + 
	        " WHERE " + DatabaseHelper.DB_TABLE_NAME + "." +
	        DatabaseHelper._ID + " = %s";
        
        Cursor thisCursor = db.rawQuery(String.format(sql, thisId), null);
        thisCursor.moveToFirst();
        
        thisCard.put("ID", "" + thisId);
        thisCard.put("english", thisCursor.getString(0));
        thisCard.put("arabic", thisCursor.getString(1));
        // card status might be null if the card hasn't been seen yet
        if (thisCursor.getString(2) == null) {
        	thisCard.put("status", "0");
        } else {
        	thisCard.put("status", thisCursor.getString(2));
        }
//        
        Log.d(TAG, "getCard: english=" + thisCursor.getString(0));
        Log.d(TAG, "getCard: arabic=" + thisCursor.getString(1));
        Log.d(TAG, "getCard: status=" + thisCursor.getString(2));
        
        thisCursor.close();
        
        if (addToHistory) {
            // add word to the card history
            cardHistory.add(thisId);
        }
        
        return thisCard;
    }
*/
    
    /**
     * Update the status of the current card
     * @param currentCardId
     * @param currentCardStatus
     * @param direction
     */
    void updateCardStatus(String currentCardId, int currentCardStatus, String direction) {
        // update the card's rank
        if (direction == "right") {
            // if a card's status is unseen
            if (currentCardStatus == 0) {
                // change the status to seen
                changeCardStatus(currentCardId, 2);
            }
        } else if (direction == "up") {
            // change the status to known
            changeCardStatus(currentCardId, 3);
        } else if (direction == "down") {
            // change the status to unknown
            changeCardStatus(currentCardId, 1);
        }
    }
    
    private void changeCardStatus(String thisCardId, int newStatus) {
//
        Log.d(TAG, "changeCardStatus: thisCardId=" + thisCardId);
        Log.d(TAG, "changeCardStatus: newStatus=" + newStatus);
                
        ContentValues cv=new ContentValues();
        cv.put(ProfileDatabaseHelper.CARD_ID, thisCardId);
        cv.put(ProfileDatabaseHelper.STATUS, newStatus);
        
        db.replace(profileName, ProfileDatabaseHelper.CARD_ID, cv);
        
        final String selection = DatabaseHelper.ENGLISH + " = ?";
        String[] selectionArgs = {thisCardId};
        
        // get all duplicates with the same card ID
        Cursor thisCursor = db.query(DatabaseHelper.DB_TABLE_NAME, new String[] {DatabaseHelper._ID}, selection, selectionArgs, null, null, null);
        // if we find any
        if (thisCursor.getCount() > 0) {
            // update the status for each duplicate
            while (thisCursor.moveToNext()) {
                cv.put(ProfileDatabaseHelper.CARD_ID, thisCursor.getString(0));
                db.replace(profileName, ProfileDatabaseHelper.CARD_ID, cv);
            }
        }
        
        thisCursor.close();
    }
}
