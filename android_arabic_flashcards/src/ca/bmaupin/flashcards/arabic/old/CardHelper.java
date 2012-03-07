package ca.bmaupin.flashcards.arabic.old;

import java.util.HashMap;
import java.util.Map;

import ca.bmaupin.flashcards.arabic.R;
import ca.bmaupin.flashcards.arabic.R.string;
import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

public class CardHelper {
    private static final String PROFILE_DB = "profileDb";
    private static final String TAG = "CardHelper";
    
    private boolean askCardOrder = true;
    public String cardOrder;
    private Context context;
    private String currentCardSet = "All";
    private String currentCardSubset = "";
    private Cursor cursor = null;
    private CardDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private String profileName = "";
    
    /**
     * @return the askCardOrder
     */
    public boolean isAskCardOrder() {
        return askCardOrder;
    }

    /**
     * @param askCardOrder the askCardOrder to set
     */
    public void setAskCardOrder(boolean askCardOrder) {
        this.askCardOrder = askCardOrder;
    }
    
    /**
     * @return the cardOrder
     */
    public String getCardOrder() {
        return cardOrder;
    }

    /**
     * @param cardOrder the cardOrder to set
     */
    public void setCardOrder(String cardOrder) {
        this.cardOrder = cardOrder;
    }
    
    /**
     * @return the profileName
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * @param profileName the profileName to set
     */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
    public CardHelper(Context context) {
    	this(context, "");
    }
    
    public CardHelper(Context context, String profileName) {
    	ProfileDatabaseHelper profileHelper = new ProfileDatabaseHelper(context);
    	
    	this.context = context;
    	// set the default card order
    	this.cardOrder = context.getString(R.string.card_order_in_order);
    	
    	Log.d(TAG, "1 cardOrder=" + cardOrder);
    	
    	// set the profile table name, which will ensure the profile table exists
    	profileHelper.setProfileTableName(profileName);

    	if (profileName.equals("")) {
    	    this.profileName = profileHelper.getProfileTableName();
    	} else {
    	    this.profileName = profileName;
    	}

    	profileHelper.close();
        
        dbHelper = new CardDatabaseHelper(context);
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
    
    void loadCardSet(String cardSet) {
        currentCardSet = cardSet;
        // close the cursor so we'll reload it with the new card set
        cursor.close();
    }
    
    void loadCardSet(String cardSet, String cardSubset) {
        currentCardSet = cardSet;
        currentCardSubset = cardSubset;
        // close the cursor so we'll reload it with the new card set
        cursor.close();
    }
    
    void startOver() {
        // if the cursor isn't null or already closed
        if (cursor != null && !cursor.isClosed()) {
            // close the cursor so we create a new one to start over
            cursor.close();
        }
    }

    void loadCardsCursor() {
        String sqlCardSetSelection = "";
        
        if (currentCardSet.equals(context.getString(
                R.string.card_group_ahlan_wa_sahlan))) {
            sqlCardSetSelection = " WHERE " + CardDatabaseHelper.CARDS_TABLE + "." + 
                    CardDatabaseHelper._ID + " IN (SELECT " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " FROM " + 
                    CardDatabaseHelper.AWS_CHAPTERS_TABLE + " WHERE " + 
                    CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = " + 
                    currentCardSubset + ") ";
            
        } else if (currentCardSet.equals(context.getString(
                R.string.card_group_categories))) {
            sqlCardSetSelection = " WHERE category = '" + currentCardSubset + 
            "'";
            
        } else if (currentCardSet.equals(context.getString(
                R.string.card_group_parts_of_speech))) {
            sqlCardSetSelection = " WHERE type = '" + currentCardSubset + 
            "'";
            
        }
    	
        String sql = "SELECT " + CardDatabaseHelper.CARDS_TABLE + "." + 
            CardDatabaseHelper._ID + ", " +
            CardDatabaseHelper.CARDS_ENGLISH + ", " +
            CardDatabaseHelper.CARDS_ARABIC + ", " +
            CardDatabaseHelper.CARDS_PLURAL + ", " +
            ProfileDatabaseHelper.STATUS +
            " FROM " + CardDatabaseHelper.CARDS_TABLE +
            " LEFT JOIN " + PROFILE_DB + "." + profileName + 
            " ON " + CardDatabaseHelper.CARDS_TABLE + "." + BaseColumns._ID
            + " = " + PROFILE_DB + "." +
            profileName + "." + 
            ProfileDatabaseHelper.CARD_ID + " %s ORDER BY ";
        
        Log.d(TAG, "2 cardOrder=" + cardOrder);
        Log.d(TAG, "R.string.card_order_in_order=" + context.getString(R.string.card_order_in_order));
        
        
        if (cardOrder.equals(context.getString(R.string.card_order_random))) {
            sql += "RANDOM()";
            if (askCardOrder) {
                // reset the value so we don't shuffle every time
                cardOrder = context.getString(
                        R.string.preferences_default_card_order_default);
            }
        } else if (cardOrder.equals(context.getString(
                R.string.card_order_in_order))) {
            // we have to massage query to maintain the same card order of the 
            // separate table where we store the ahlan wa sahlan data 
            if (currentCardSet.equals(context.getString(
                    R.string.card_group_ahlan_wa_sahlan))) {
                sqlCardSetSelection = " LEFT JOIN " + 
                        CardDatabaseHelper.AWS_CHAPTERS_TABLE + " ON " + 
                        CardDatabaseHelper.CARDS_TABLE + "." + CardDatabaseHelper._ID + 
                        " = " + CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." +
                        CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " " +
                        sqlCardSetSelection +
                        " AND " + CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." + 
                        CardDatabaseHelper._ID + " IN (SELECT " + 
                        CardDatabaseHelper._ID +
                        " FROM " + CardDatabaseHelper.AWS_CHAPTERS_TABLE + 
                        " WHERE " +
                        CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = " +
                        currentCardSubset + ") ";
                sql += CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." + 
                        CardDatabaseHelper._ID;
                Log.d(TAG, "card order is in order");
            }
/* disable smart mode            
            else {
                sql += DatabaseHelper.CARDS_TABLE + "." + DatabaseHelper._ID;
            }
*/
            
            if (askCardOrder) {
                // reset the value so we don't go in order every time
                cardOrder = context.getString(
                        R.string.preferences_default_card_order_default);
            }
        } else {
            // secondary random sort so it's not always in the same order
            sql += ProfileDatabaseHelper.STATUS + ", RANDOM()";
        }
        
        Log.d(TAG, "rawQuery=" + String.format(sql, sqlCardSetSelection));
        
        cursor = db.rawQuery(String.format(sql, sqlCardSetSelection), null);
    }
    
    Map<String, String> nextCard() {
//
    	Log.d(TAG, "nextCard called");

    	// the cursor will be null if we haven't created one yet
    	// it will be closed if we're changing categories or starting over
    	if (cursor == null || cursor.isClosed()) {
    	    loadCardsCursor();
    	}
    	
    	if (cursor.isLast() || cursor.getCount() == 0) {
    	    // return a blank card so we can show the user a message that
            // there aren't any more cards (or any cards at all)
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
        
        thisCard.put("ID", cursor.getString(0));
        thisCard.put("english", cursor.getString(1));
        thisCard.put("arabic", cursor.getString(2));
        thisCard.put("plural", cursor.getString(3));
        // card status might be null if the card hasn't been seen yet
        if (cursor.getString(4) == null) {
        	thisCard.put("status", "0");
        } else {
        	thisCard.put("status", cursor.getString(4));
        }
//        
        Log.d(TAG, "getCurrentCard: _id=" + cursor.getString(0));
        Log.d(TAG, "getCurrentCard: english=" + cursor.getString(1));
        Log.d(TAG, "getCurrentCard: arabic=" + cursor.getString(2));
        Log.d(TAG, "getCurrentCard: plural=" + cursor.getString(3));
        Log.d(TAG, "getCurrentCard: status=" + cursor.getString(4));
        
        return thisCard;
    }

    Map<String, String> getCardById(String thisId) {
        Log.d(TAG, "getCard: thisId=" + thisId);
        Map<String, String> thisCard = new HashMap<String, String>();
        
        String sql = "SELECT " +
            CardDatabaseHelper.CARDS_ENGLISH + ", " +
            CardDatabaseHelper.CARDS_ARABIC + ", " +
            CardDatabaseHelper.CARDS_PLURAL + ", " +
            ProfileDatabaseHelper.STATUS +
            " FROM " + CardDatabaseHelper.CARDS_TABLE +
            " LEFT JOIN " + PROFILE_DB + "." + profileName +
            " ON " + CardDatabaseHelper.CARDS_TABLE + "." + BaseColumns._ID
            + " = " + PROFILE_DB + "." +
            profileName + "." + 
            ProfileDatabaseHelper.CARD_ID + 
            " WHERE " + CardDatabaseHelper.CARDS_TABLE + "." +
            CardDatabaseHelper._ID + " = %s";
        
        Cursor thisCursor = db.rawQuery(String.format(sql, thisId), null);
        thisCursor.moveToFirst();
        
        thisCard.put("ID", "" + thisId);
        thisCard.put("english", thisCursor.getString(0));
        thisCard.put("arabic", thisCursor.getString(1));
        thisCard.put("plural", thisCursor.getString(2));
        // card status might be null if the card hasn't been seen yet
        if (thisCursor.getString(3) == null) {
            thisCard.put("status", "0");
        } else {
            thisCard.put("status", thisCursor.getString(3));
        }
//        
        Log.d(TAG, "getCard: english=" + thisCursor.getString(0));
        Log.d(TAG, "getCard: arabic=" + thisCursor.getString(1));
        Log.d(TAG, "getCard: plural=" + thisCursor.getString(2));
        Log.d(TAG, "getCard: status=" + thisCursor.getString(3));
        
        thisCursor.close();
        
        return thisCard;
    }
    
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
    }
    
    void deleteProfile() {
        deleteProfile(profileName);
    }
    
    void deleteProfile(String profileName) {
        String sql = "DELETE FROM " + PROFILE_DB + "." + profileName;
        db.execSQL(sql);
        startOver();
        ProfileDatabaseHelper profileHelper = new ProfileDatabaseHelper(context);
        SQLiteDatabase profileDb = profileHelper.getReadableDatabase();
        profileDb.execSQL("VACUUM");
        profileDb.close();
        profileHelper.close();
    }
}
