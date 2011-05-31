package us.bmaupin.flashcards.arabic;

// $Id$

/*
 * TODO
 * 1. rethink card getting paradigm
 * 		better just to create query, get unseen cards first, then...
 * 		that way we could just get next, and not have to create a new query...
 * 2. fix categories, AGAIN :)
 * 3. what to do when we get to X cards (100?)
 * 4. test getting status with card vs. setting string for current
 * status ("unkown", "unseen", etc.)
 * 		going through history would need a method for updating status
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import us.bmaupin.test.DatabaseHelper;
//import us.bmaupin.test.ProfileDatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class CardHelper {
    private static final String TAG = "CardHelper";
    private Cursor cursor = null;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;    
    private List<String> cardHistory = new ArrayList<String>();
    private int cardHistoryIndex = 0;
    private boolean categoryChanged = false;
    private String currentCategory = "All";
    private String currentStatus = "";
    private String currentSubCategory = "";
//    private List<Integer> currentCardIds = new ArrayList<Integer>();
    private static final String PROFILE_DB = "profileDb";
    private String profileName = "";
    
    public CardHelper(Context context) {
    	this(context, "");
    }
    
    public CardHelper(Context context, String profileName) {
// TODO: would this be better elsewhere?
    	ProfileDatabaseHelper profileHelper = new ProfileDatabaseHelper(context);
// TODO: could this be cleaner/better?
/*
 * create a constant in ProfileDatabaseHelper for the default profile name?
 */
    	if (profileName.equals("")) {
    		// make sure the profile table exists
    		SQLiteDatabase profileDb = profileHelper.getReadableDatabase();
    		this.profileName = profileHelper.getprofileTableName();
    		profileDb.close();
    	} else {
    		this.profileName = profileName;
    		// make sure the profile table exists
    		SQLiteDatabase profileDb = profileHelper.getReadableDatabase(profileName);
    		profileDb.close();
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
    
/*
// TODO: remove categoryChanged if we're not using it
    private void loadCards(boolean categoryChanged) {
        Log.d(TAG, "loadCards called");
//        List<Integer> currentCardIds = new ArrayList<Integer>();
        
        // these need to be emptied each time loadCards is called
// TODO: do we need to clear anything here?
//        currentRankedIds.clear();
        
// TODO: we need to fix category selection.  AGAIN        
        String[] columns = { "_ID" };
        String selection = null;
        
        if (currentCategory.equals("Ahlan wa sahlan")) {
            selection = "aws_chapter = " + currentSubCategory;
        }

//      cursor = wordsDb.query("words", columns, selection, null, null, null, null);
        /*
        String sql = "SELECT _ID FROM " + DatabaseHelper.DB_TABLE_NAME +
            " WHERE _ID IN (SELECT _ID FROM " + PROFILE_DB + "." + 
            ProfileDatabaseHelper.DB_TABLE_NAME + " WHERE " + 
            ProfileDatabaseHelper.STATUS + " = %d);";
        */
/*        
        String sql = "SELECT " + DatabaseHelper.DB_TABLE_NAME + "." + 
                DatabaseHelper._ID +
                " FROM " + DatabaseHelper.DB_TABLE_NAME +
                " LEFT JOIN profileDb." + profileName + 
                " ON " + DatabaseHelper.DB_TABLE_NAME + "." + BaseColumns._ID
                + " = profileDb." + 
                profileName + "." + 
                ProfileDatabaseHelper.CARD_ID + 
                " WHERE " + ProfileDatabaseHelper.STATUS + " IS NULL";
        
        /*
         * 1. flag string to tell us what status we're showing
         * 	upside: don't have to get status
         * 	downside: won't work with history
         * 2. get status when we get card
         *  why not?
         */
/*        
        // get all unread cards (where status == 0)
        Cursor cursor = db.rawQuery(String.format(sql, 0), null);
        cursor.moveToFirst();
        
// TODO: for now, only get 5 cards
        for (int i=1; i<6; i++) {
//        while (cursor.moveToNext()) {
            int thisId = cursor.getInt(0);
            currentCardIds.add(thisId);
// testing
            cursor.moveToNext();
        }
        
        cursor.close();
    }
*/
    
    
    void loadCategory(String category) {
        currentCategory = category;
        // set category changed flag so we know to reload the cards cursor
        categoryChanged = true;
//        loadCardsCursor();
    }
    
    void loadCategory(String category, String subCategory) {
        currentCategory = category;
        currentSubCategory = subCategory;
        // set category changed flag so we know to reload the cards cursor
        categoryChanged = true;
//        loadCardsCursor();
    }    

    void loadCardsCursor() {
        String sqlCategorySelection = "";
//        String sqlStatusSelection = "";
        
        if (currentCategory.equals("Ahlan wa sahlan")) {
            sqlCategorySelection = "WHERE aws_chapter = " + currentSubCategory;
        }
        
// TODO: fix category selection, AGAIN
    	
/*
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
	        ProfileDatabaseHelper.CARD_ID + 
	        " WHERE " + ProfileDatabaseHelper.STATUS + "%s %s";
	    
	    if (currentStatus.equals("unseen")) {
	    	sqlStatusSelection = " IS NULL";
	    } else if (currentStatus.equals("unknown")) {
	    	sqlStatusSelection = " = 1";
	    } else if (currentStatus.equals("seen")) {
	    	sqlStatusSelection = " = 2";
	    } else if (currentStatus.equals("known")) {
            sqlStatusSelection = " = 3";
        }
	    
	    Log.d(TAG, "rawQuery=" + String.format(sql, sqlStatusSelection, 
                sqlCategorySelection));
	    
	    cursor = db.rawQuery(String.format(sql, sqlStatusSelection, 
	            sqlCategorySelection), null);
*/
        
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
	    
	    cursor.moveToFirst();
// TODO: I'm pretty sure we're not correctly handling queries that don't return any results.  need to either handle here or in next/prevCard
    }

    Map<String, String> nextCard() {
    	return nextCard(false);
    }
/*
    void nextStatus() {
        if (currentStatus.equals("")) {
            currentStatus = "unseen";
        } else if (currentStatus.equals("unseen")) {
            currentStatus = "unknown";
        } else if (currentStatus.equals("unknown")) {
            currentStatus = "seen";
        } else if (currentStatus.equals("seen")) {
            currentStatus = "known";
        }
    }
*/
    
    /***
     * Get a card given its ID and a boolean value determining whether or not
     * it should be added to the card history
     * @param thisId
     * @param addToHistory
     * @return
     */
    Map<String, String> nextCard(boolean addToHistory) {
//        Log.d(TAG, "getCard: thisId=" + thisId);
    	Log.d(TAG, "nextCard called");
        
        Map<String, String> thisCard = new HashMap<String, String>();
        
        /* 
         * select words._id as _id,english,arabic,status from words 
         * left join profiledb.profile1 on words._id = 
         * profiledb.profile1.card_id where status is not null;"
         */

/*
        // create a new cursor if necessary
//        if (categoryChanged || cursor == null || cursor.isClosed()) {
//        if (cursor == null || cursor.isClosed()) {
// TODO: in the future, do we want to put all this into some kind of list/array?        
//            String[] columns = { "_ID", "english", "arabic" };
//            String[] columns = { "_ID" };
//            String selection = null;
            
            if (currentCategory.equals("Ahlan wa sahlan")) {
                selection = "aws_chapter = " + currentSubCategory;
            }

        }
*/

// TODO: reimplement card history

// TODO: I'm pretty sure we're not correctly handling queries that don't return any results.  need to either handle here or in loadCardsCursor
        if (cursor == null) {
//            nextStatus();
        	loadCardsCursor();
        } else if (categoryChanged) {
            // reset category changed flag
            categoryChanged = false;
            currentStatus = "unseen";
            loadCardsCursor();
        } else {
        	if (!cursor.isLast()) {
        		cursor.moveToNext();
        	} else {
        		cursor.close();
// TODO: here handle if status is seen (prompt user if he/she wants to see known)
// TODO: here handle if status is known (end of known cards)
//        		nextStatus();
        		loadCardsCursor();
        	}
        }
        
        thisCard.put("ID", "" + cursor.getString(0));
        thisCard.put("english", cursor.getString(1));
        thisCard.put("arabic", cursor.getString(2));
        // card status might be null if the card hasn't been seen yet
        if (cursor.getString(3) == null) {
        	thisCard.put("status", "0");
        } else {
        	thisCard.put("status", cursor.getString(3));
        }
//        
        Log.d(TAG, "getCard: _id=" + cursor.getString(0));
        Log.d(TAG, "getCard: english=" + cursor.getString(1));
        Log.d(TAG, "getCard: arabic=" + cursor.getString(2));
        Log.d(TAG, "getCard: status=" + cursor.getString(3));
        
        if (addToHistory) {
            // add word to the card history
            cardHistory.add(cursor.getString(0));
        }
        
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
    
/*    
    Map<String, String> nextCard() {
//        
        Log.d(TAG, "nextCard called");
        Log.d(TAG, "nextCard: cardHistoryIndex=" + cardHistoryIndex);
        Log.d(TAG, "nextCard: cardHistory=" + cardHistory);
        
        // if we're going forward through the card history
        if (cardHistoryIndex > 0) {
            cardHistoryIndex --;
//
            Log.d(TAG, "nextCard: new cardHistoryIndex=" + cardHistoryIndex);
            // get the next card in the card history
            int thisId = cardHistory.get(cardHistory.size() - (cardHistoryIndex + 1));
            
            return getCard(thisId, false);
            
        } else if (!currentCardIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentCardIds.remove(0);
            return getCard(thisId, true);
            
// TODO: here, first ask the user if they want to see known cards
        
        // if we've no more cards
        } else {
            // load more
            loadCards(false);
            return nextCard();
        }
        
    }
*/
    
// TODO: prevCard needs to be fixed
    Map<String, String> prevCard() {
//
        Log.d(TAG, "prevCard: cardHistoryIndex=" + cardHistoryIndex);
        Log.d(TAG, "prevCard: cardHistory=" + cardHistory);
        
        // if we have anything in card history and we're not at the last card in the history
        if (cardHistory.size() > 1 && cardHistoryIndex < cardHistory.size() - 1) {
            cardHistoryIndex++;
//
            Log.d(TAG, "prevCard: new cardHistoryIndex=" + cardHistoryIndex);
            // get the previous card in the card history
//            int thisId = cardHistory.get(cardHistory.size() - (cardHistoryIndex + 1));
//            return getCard(thisId, false);
            return new HashMap<String, String>();
            
        // if cardHistory is empty or we're at the last card in the history
        } else {
            return new HashMap<String, String>();
        }
    }
    
    /**
     * Update the status of the current card
     * @param currentCardId
     * @param currentCardRank
     * @param direction
     */
    void updateCardStatus(String currentCardId, int currentCardStatus, String direction) {
        // if we're not going through the card history
        if (cardHistoryIndex < 1) {
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
    }
    
    private void changeCardStatus(String thisCardId, int newStatus) {
//
        Log.d(TAG, "changeCardStatus: thisCardId=" + thisCardId);
        Log.d(TAG, "changeCardStatus: newStatus=" + newStatus);
                
        ContentValues cv=new ContentValues();
        cv.put(ProfileDatabaseHelper.CARD_ID, thisCardId);
        cv.put(ProfileDatabaseHelper.STATUS, newStatus);
        
        db.replace(profileName, ProfileDatabaseHelper.CARD_ID, cv);
    }
}
