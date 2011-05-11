package us.bmaupin.flashcards.arabic;

// $Id$

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
//    private DatabaseHelper wordsHelper;
//    private SQLiteDatabase wordsDb;
    private Cursor cursor = null;
//    private ProfileDatabaseHelper profileHelper;
//    private SQLiteDatabase profileDb;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;    
    private List<Integer> cardHistory = new ArrayList<Integer>();
    private int cardHistoryIndex = 0;
//    private int rankedCardsShown = 0;
    private String currentCategory = "All";
    private String currentSubCategory;
    private List<Integer> currentCardIds = new ArrayList<Integer>();
    //private List<Integer> currentKnownIds = new ArrayList<Integer>();
    //private List<Integer> currentSeenIds = new ArrayList<Integer>();
    //private List<Integer> currentUnknownIds = new ArrayList<Integer>();
    //private List<Integer> currentUnseenIds = new ArrayList<Integer>();
    private static final String PROFILE_DB = "profileDb";
    private String profileName = "";
//    private WeightedRandomGenerator weightedCardIds;
    
    public CardHelper(Context context) {
    	this(context, "");
    }
    
    public CardHelper(Context context, String profileName) {
        /*
        wordsHelper = new DatabaseHelper(context);
        wordsDb = wordsHelper.getReadableDatabase();
        
//        ranksHelper = new RankDatabaseHelper(context);
//        ranksDb = ranksHelper.getReadableDatabase();
        */

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
// TODO: we need a way to make sure the profile db is initialized
        db.execSQL("attach database ? as " + PROFILE_DB, 
            new String[] {context.getDatabasePath(ProfileDatabaseHelper.DATABASE_NAME).getPath()});
    }
    
    public void close() {
        // clean up after ourselves
        cursor.close();
        db.close();
        dbHelper.close();
//        profileHelper.close();
//        wordsHelper.close();
    }
    
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
        
//        loadCardPriorities(currentCardIds);
    }
    
    /*
    private void loadCardPriorities(List<Integer> currentCardIds) {
        // for each card ID in current cards
        for (int thisId : currentCardIds) {
            // get its status
            int thisStatus = getCardStatus(thisId);
            
            // if the rank for this particular card is 0
            if (thisStatus == 0) {
                // add it to the list of cards we haven't seen yet
                currentUnseenIds.add(thisId);
            } else if (thisStatus == 1) {
                // if it's 1, add it to the list of cards marked as unknown
                currentUnknownIds.add(thisId);
            } else if (thisStatus == 2) {
                // 2, add it to the list of seen cards
                currentSeenIds.add(thisId);
            } else if (thisStatus == 3) {
                // 3, add it to the list of cards marked as known
                currentKnownIds.add(thisId);
            }
        }
    }
    */
    
    /*
    // loadCards in arabicFlashcards should prob be called something like loadViews
    private void loadCards(boolean categoryChanged) {
        Log.d(TAG, "loadCards called");
        List<Integer> currentCardIds = new ArrayList<Integer>();
        List<Integer> currentOrderedRanks = new ArrayList<Integer>();
        
        // these need to be emptied each time loadCards is called
        currentRankedIds.clear();
        currentUnrankedIds.clear();
        
        // create a new cursor if necessary
        if (categoryChanged || cursor == null || cursor.isClosed()) {
// TODO: in the future, do we want to put all this into some kind of list/array?        
//            String[] columns = { "_ID", "english", "arabic" };
            String[] columns = { "_ID" };
            String selection = null;
            
            if (currentCategory.equals("Ahlan wa sahlan")) {
                selection = "aws_chapter = " + currentSubCategory;
            }
            
            cursor = wordsDb.query("words", columns, selection, null, null, null, null);
            cursor.moveToFirst();
        }
        
// TODO: for now, only get 5 cards
        // only get 100 cards at a time
        for (int i=1; i<6; i++) {
            int thisId = cursor.getInt(0);
            currentCardIds.add(thisId);
            
// TODO: we're probably going to run into issues if we have, for instance, 103 cards on the second goaround
            // if there are no more cards to get
            if (!cursor.moveToNext()) {
                // close the cursor and move along
                cursor.close();
                break;
            }
        }
        
        currentOrderedRanks = loadRanks(currentCardIds);

        if (!currentOrderedRanks.isEmpty()) {
            weightedCardIds = new WeightedRandomGenerator(currentOrderedRanks);
        }
        
//        
        Log.d(TAG, "loadCards: currentOrderedRanks:");
        for (int thisRank : currentOrderedRanks) { 
            Log.d(TAG, "" + thisRank);
        }
//        
        Log.d(TAG, "loadCards: currentRankedIds:");
        for (int thisID : currentRankedIds) {
            Log.d(TAG, "" + thisID);
        }
//        
        Log.d(TAG, "loadCards: currentUnrankedIds:");
        for (int thisID : currentUnrankedIds) {
            Log.d(TAG, "" + thisID);
        }
    }
    */
    
    void loadCategory(String category) {
        currentCategory = category;
        loadCards(true);
    }
    
    void loadCategory(String category, String subCategory) {
        currentCategory = category;
        currentSubCategory = subCategory;
        loadCards(true);
    }    
    
    /*
    private List<Integer> loadRanks(List<Integer> currentCardIds) {
        Map<Integer, Integer> currentCardRanks = new HashMap<Integer, Integer>();
        List<Integer> currentOrderedRanks = new ArrayList<Integer>();
        String[] columns = { "rank" };
        
        // for each card ID in current cards
        for (int thisId : currentCardIds) {
            String selection = "_ID = " + thisId;
            // get its rank
            Cursor thisCursor = ranksDb.query("ranks", columns, selection, null, null, null, null);
            thisCursor.moveToFirst();
            int thisRank = thisCursor.getInt(0);
            thisCursor.close();
            
            // if the rank for this particular card is 0
            if (thisRank == 0) {
                // add it to the list of cards with no rank
                currentUnrankedIds.add(thisId);
            } else {
                // put it in currentCardRanks
                currentCardRanks.put(thisId, thisRank);
            }
        }
        
        if (!currentCardRanks.isEmpty()) {
//            
            Log.d(TAG, "loadRanks: processing ranks");
            
            // the binary search function we'll be using later needs this to be sorted
            currentCardRanks = sortByValue(currentCardRanks);
            
            // for each card and its rank
            for (Map.Entry<Integer, Integer> entry : currentCardRanks.entrySet()) {
                // put the id into the list of ranked ids
                currentRankedIds.add(entry.getKey());
                // put the rank into the list of ranks
                currentOrderedRanks.add(entry.getValue());
            }
        }
            
        return currentOrderedRanks;
    }
    
    private class WeightedRandomGenerator {
        private List<Double> totals = new ArrayList<Double>();
        
        private WeightedRandomGenerator(List<Integer> weights) {
            double runningTotal = 0;
            
            for (int thisRank: weights) {
                runningTotal += thisRank;
                totals.add(runningTotal);
            }
        }
        
        private int next() {
            Random rnd = new Random(System.nanoTime());
            Double rndNum = rnd.nextDouble() * totals.get(totals.size() - 1);
            int sNum = Collections.binarySearch(totals, rndNum);
            int idx = (sNum < 0) ? (Math.abs(sNum) - 1) : sNum;
            return idx;
        }
    }
    */
    
    /***
     * Get a card given it's ID and a boolean value determining whether or not
     * it should be added to the card history
     * @param thisId
     * @param addToHistory
     * @return
     */
    private Map<String, String> getCard(int thisId, boolean addToHistory) {
        Log.d(TAG, "getCard: thisId=" + thisId);
        
//        String[] columns = { "english", "arabic" };
//        String[] columns = { DatabaseHelper.ENGLISH, DatabaseHelper.ARABIC, ProfileDatabaseHelper.STATUS };
//        String selection = "_ID = ?";
//        String[] selectionArgs = new String[1];
        Map<String, String> thisCard = new HashMap<String, String>();
        
        /*
        selectionArgs[0] = "" + thisId;
        Cursor thisCursor = db.query("words", columns, selection, selectionArgs, null, null, null);
        */
        
        String sql = "SELECT " +
//        	+ DatabaseHelper.DB_TABLE_NAME + "." + 
//        DatabaseHelper._ID +
        DatabaseHelper.ENGLISH + ", " +
        DatabaseHelper.ARABIC + ", " +
        ProfileDatabaseHelper.STATUS +
        " FROM " + DatabaseHelper.DB_TABLE_NAME +
        " LEFT JOIN profileDb." + profileName + 
        " ON " + DatabaseHelper.DB_TABLE_NAME + "." + BaseColumns._ID
        + " = profileDb." + 
        profileName + "." + 
        ProfileDatabaseHelper.CARD_ID + 
        " WHERE " + DatabaseHelper.DB_TABLE_NAME + "." +
        DatabaseHelper._ID + " = %s";
//        ProfileDatabaseHelper.STATUS + " IS NULL;";
        
        Cursor thisCursor = db.rawQuery(String.format(sql, thisId), null);
        
        thisCursor.moveToFirst();
        
//        String english = thisCursor.getString(0);
//        String arabic = thisCursor.getString(1);
        
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
    
    /*
    private int getCardStatus(String thisId) {
        String[] columns = { ProfileDatabaseHelper.STATUS };
        String selection = "_ID =" + thisId;
        int thisStatus;
        // get its status
//        Cursor thisCursor = profileDb.query(profileHelper.getProfileName(), columns, selection, null, null, null, null);
//        Cursor thisCursor = profileDb.query(ProfileDatabaseHelper.DB_TABLE_NAME, columns, selection, null, null, null, null);
        Cursor thisCursor = db.query(ProfileDatabaseHelper.DB_TABLE_NAME, columns, selection, null, null, null, null);
        thisCursor.moveToFirst();
        try {
            thisStatus = thisCursor.getInt(0);
        // if the status for this card isn't in the database
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            Log.d(TAG, "CursorIndexOutOfBoundsException: " + e);
            // set it to 0 (unseen)
            thisStatus = 0;
        }
        thisCursor.close();
        
        return thisStatus;
    }
    
    private int getCardStatus(int thisId) {
        return getCardStatus("" + thisId);
    }
    */
    
    Map<String, String> nextCard() {
//        
        Log.d(TAG, "nextCard called");
        Log.d(TAG, "nextCard: cardHistoryIndex=" + cardHistoryIndex);
        Log.d(TAG, "nextCard: cardHistory=" + cardHistory);
//        Log.d(TAG, "nextCard: rankedCardsShown=" + rankedCardsShown);
        
        // if we're going forward through the card history
        if (cardHistoryIndex > 0) {
            cardHistoryIndex --;
//
            Log.d(TAG, "nextCard: new cardHistoryIndex=" + cardHistoryIndex);
            // get the next card in the card history
            int thisId = cardHistory.get(cardHistory.size() - (cardHistoryIndex + 1));
//            Map<String, String> thisCard = getCard(thisId, false);
// TODO: this seems messy; most of the time getCard is called, we want the rank...
            // update its status
//            thisCard.put("status", "" + getCardStatus(thisCard.get("ID")));
            // return it
//            return thisCard;
            
            return getCard(thisId, false);
            
        /*
        // first show all the unseen cards
        } else if (!currentUnseenIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentUnseenIds.remove(0);
            Map<String, String> thisCard = getCard(thisId, true);
            thisCard.put("status", "0");
            return thisCard;
        
        // then show the unknown cards
        } else if (!currentUnknownIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentUnknownIds.remove(0);
            Map<String, String> thisCard = getCard(thisId, true);
            thisCard.put("status", "1");
            return thisCard;

        // next show the seen cards
        } else if (!currentSeenIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentSeenIds.remove(0);
            Map<String, String> thisCard = getCard(thisId, true);
            thisCard.put("status", "2");
            return thisCard;

// TODO: here, first ask the user if they want to see known cards
        // lastly show the known cards
        } else if (!currentKnownIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentKnownIds.remove(0);
            Map<String, String> thisCard = getCard(thisId, true);
            thisCard.put("status", "3");
            return thisCard;
        */
            
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
    
    /*
    Map<String, String> nextCardOld() {
        Log.d(TAG, "nextCard called");
//        
        Log.d(TAG, "nextCard: cardHistoryIndex=" + cardHistoryIndex);
        Log.d(TAG, "nextCard: cardHistory=" + cardHistory);
        Log.d(TAG, "nextCard: rankedCardsShown=" + rankedCardsShown);
        
        // if we're going forward through the card history
        if (cardHistoryIndex > 0) {
            cardHistoryIndex --;
//
            Log.d(TAG, "nextCard: new cardHistoryIndex=" + cardHistoryIndex);
            // get the next card in the card history
            int thisId = cardHistory.get(cardHistory.size() - (cardHistoryIndex + 1));
            Map<String, String> thisCard = getCard(thisId, false);
// TODO: this seems messy; most of the time getCard is called, we want the rank...
            // update its rank
            thisCard.put("rank", "" + getRank(thisCard.get("ID")));
            // return it
            return thisCard;

        // if some of the selected cards don't have ranks, that means they 
        // haven't been shown yet, so show them
        } else if (!currentUnrankedIds.isEmpty()) {
            // remove the first element from the list
            int thisId = currentUnrankedIds.remove(0);
            Map<String, String> thisCard = getCard(thisId, true);
            thisCard.put("rank", "0");
            return thisCard;

        } else if (rankedCardsShown < currentRankedIds.size()) {
//            
            Log.d(TAG, "nextCard: currentRankedIds.size()=" + currentRankedIds.size());
            Log.d(TAG, "nextCard: currentUnrankedIds.size()=" + currentUnrankedIds.size());
            
            int thisId = 0;

// TODO: we might need to make sure that there are more than 5 cards to show in the first place
            // try 5 times to get a card that isn't one of the last 5 shown
            for (int i=1; i<7; i++) {
//                
                Log.d(TAG, "nextCard: i=" + i);
                
                // on the 6th try
                if (i == 6) {
                    Random rnd = new Random(System.nanoTime());
                    // return a random card
                    thisId = currentRankedIds.get((int)(rnd.nextDouble() * (currentRankedIds.size())));
                    break;
                }
                
                // get the next weighted card ID
                thisId = currentRankedIds.get(weightedCardIds.next());
                
                // if there is no card history, then we're good to go
                if (cardHistory.size() == 0) {
                    break;
                // make sure the card history is at least 5
                } else if (cardHistory.size() < 5) {
                    // if it's less than 5, just make sure the card isn't in the history
                    if (!cardHistory.contains(thisId)) {
                        break;
                    }
                // if this ID isn't one of the last 5 shown
                } else if (!cardHistory.subList(cardHistory.size() - 5, cardHistory.size()).contains(thisId)) {
                    // show it
                    break;
                }
            }
            
            Map<String, String> thisCard = getCard(thisId, true);
            // get its rank
            thisCard.put("rank", "" + getRank(thisCard.get("ID")));
            // increment the counter of ranked cards shown
            rankedCardsShown ++;
            // return it
            return thisCard;
            
        // if we've no more cards
        } else {
            // load more
            loadCards(false);
            // reset the counter of ranked cards shown
            rankedCardsShown = 0;
            return nextCard();
        }
    }
    */
    
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
            int thisId = cardHistory.get(cardHistory.size() - (cardHistoryIndex + 1));
            return getCard(thisId, false);
            // update its status
//            thisCard.put("status", "" + getCardStatus(thisCard.get("ID")));
            // return it
//            return thisCard;
            
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
        
//        String whereClause = "_ID = ?";
//        String[] whereArgs = {thisCardId};
        
        ContentValues cv=new ContentValues();
        cv.put(ProfileDatabaseHelper.CARD_ID, thisCardId);
        cv.put(ProfileDatabaseHelper.STATUS, newStatus);
        
//        profileDb.update(profileHelper.getProfileName(), cv, whereClause, whereArgs);
        /*
        try {
        	db.update(profileName, cv, whereClause, whereArgs);
        } catch () {
        	
        }
        */
        db.replace(profileName, ProfileDatabaseHelper.CARD_ID, cv);
//        db.replace(profileName, nullColumnHack, initialValues);
    }
    
    /*
    void updateRank(String currentCardId, int currentCardRank, String direction) {
        // if we're not going through the card history
        if (cardHistoryIndex < 1) {
            // update the card's rank
            if (direction == "right") {
                updateRankNormal(currentCardId, currentCardRank);
            } else if (direction == "up") {
                updateRankKnown(currentCardId);
            } else if (direction == "down") {
                updateRankNotKnown(currentCardId, currentCardRank);
            }
        }
    }
    
    private void updateRankNormal(String thisCardId, int thisCardRank) {
        int newCardRank;
        
        // don't go any lower than 2; 1 is reserved for cards we know
        if (thisCardRank == 1 || thisCardRank == 2) {
            return;
        // if a card is unranked, set the default starting rank to 20
        } else if (thisCardRank == 0) {
            newCardRank = 20;
        // reduce the rank by 1
        } else {
            newCardRank = thisCardRank - 1;
        }
        
        String whereClause = "_ID = ?";
        String[] whereArgs = {thisCardId};
        
        ContentValues cv=new ContentValues();
        cv.put(RankDatabaseHelper.RANK, newCardRank);
        
        ranksDb.update(RankDatabaseHelper.DB_TABLE_NAME, cv, whereClause, whereArgs);
    }

    private void updateRankKnown(String thisCardId) {
        String whereClause = "_ID = ?";
        String[] whereArgs = {thisCardId};
        
        ContentValues cv=new ContentValues();
        // set the rank to 1
        cv.put(RankDatabaseHelper.RANK, 1);
        
        ranksDb.update(RankDatabaseHelper.DB_TABLE_NAME, cv, whereClause, whereArgs);
    }

    private void updateRankNotKnown(String thisCardId, int thisCardRank) {
        // if a card is unranked, set the default starting rank to 20
        if (thisCardRank == 0) {
            thisCardRank = 20;
        }
        // increase the rank by 2
        thisCardRank = thisCardRank + 2;
        
        String whereClause = "_ID = ?";
        String[] whereArgs = {thisCardId};
        
        ContentValues cv=new ContentValues();
        cv.put(RankDatabaseHelper.RANK, thisCardRank);
        
        ranksDb.update(RankDatabaseHelper.DB_TABLE_NAME, cv, whereClause, whereArgs);
    }
    
    // from http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
    static Map sortByValue(Map map) {
         List list = new LinkedList(map.entrySet());
         Collections.sort(list, new Comparator() {
              public int compare(Object o1, Object o2) {
                   return ((Comparable) ((Map.Entry) (o1)).getValue())
                  .compareTo(((Map.Entry) (o2)).getValue());
              }
         });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    */
}
