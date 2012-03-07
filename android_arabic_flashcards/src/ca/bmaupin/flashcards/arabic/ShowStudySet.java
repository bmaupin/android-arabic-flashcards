package ca.bmaupin.flashcards.arabic;

import com.googlecode.chartdroid.pie.ChartPanelActivity;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import ca.bmaupin.flashcards.arabic.data.CardProvider;
import ca.bmaupin.flashcards.arabic.data.CardQueryHelper;
import ca.bmaupin.flashcards.arabic.data.StudySetDatabaseHelper;
import ca.bmaupin.flashcards.arabic.data.StudySetHelper;
import ca.bmaupin.flashcards.arabic.data.StudySetProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ShowStudySet extends FragmentActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ShowStudySet";
    private static final int RESPONSE_KNOWN = 0;
    private static final int RESPONSE_IFFY = 1;
    private static final int RESPONSE_UNKNOWN = 2;
    private static final int CARD_MODE_DUE = 0;
    private static final int CARD_MODE_NEW = 1;
    private static final int CARD_MODE_NONE_DUE = 2;
    // constants for swipe
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final long ONE_HOUR_IN_MS = 3600000;
    
    private static final String[] PROJECTION_CARDS = new String[] {
        CardDatabaseHelper.CARDS_TABLE + "." + CardDatabaseHelper._ID,
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC,
    };
    
    private int cardMode = CARD_MODE_DUE;
    private String cardGroup;
    private String cardSubgroup;
    private Cursor cardsCursor;
    // current card language
    private String currentLang;
    // to hold the current toast so we can update it
    Toast currentToast;
    // default card language 
    private String defaultLang;
    // the number of due cards to show
    private int dueCardCount = 0;
    // whether or not to apply arabic fixes
    private Boolean fixArabic;
    private GestureDetector gestureDetector;
    private int iffyCardCount = 0;
    private int knownCardCount = 0;
    private SharedPreferences preferences;
    // how many previous cards we've gone back
    private int prevCardCount = 0;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    private int studySetId;
    private String studySetIds = "";
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;
    private int unknownCardCount = 0;
    private ViewFlipper vf;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();
        
        Bundle bundle = this.getIntent().getExtras();
        studySetId = bundle.getInt(Cards.EXTRA_STUDY_SET_ID);
        
        Cursor cursor = getContentResolver().query(
                StudySetProvider.CONTENT_URI_META,
                new String[] {StudySetDatabaseHelper.META_CARD_GROUP,
                        StudySetDatabaseHelper.META_CARD_SUBGROUP,
                        StudySetDatabaseHelper.META_SET_LANGUAGE},
                StudySetDatabaseHelper._ID + " = ? ",
                new String[] {"" + studySetId},
                null
        );
        if (cursor.moveToFirst()) { 
            cardGroup = cursor.getString(0);
            cardSubgroup = cursor.getString(1);
            defaultLang = cursor.getString(2);
        }
        cursor.close();
        
        String selection = StudySetDatabaseHelper.SET_DUE_TIME + " < " + 
                System.currentTimeMillis();
        Log.d(TAG, "System.currentTimeMillis(): " + System.currentTimeMillis());
        
        cursor = getContentResolver().query(
                // specify the study set ID and a limit
                ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                        studySetId).buildUpon().appendQueryParameter(
                        StudySetProvider.QUERY_PARAMETER_LIMIT,
                        "" + Cards.MAX_CARDS_TO_SHOW).build(),
                new String[] {StudySetDatabaseHelper.SET_CARD_ID},
                selection,
                null,
/*
 *  okay, so apparently ordering by due time here doesn't matter, because
 *  the order is ignored when getting the cards.  maybe not worth worrying 
 *  about (would probably require a join to get working)...  but we'll keep it 
 *  anyway so at least the cards due soonest will be in the set of shown cards, 
 *  even if not in order by due time
 */
                StudySetDatabaseHelper.SET_DUE_TIME);

        if (cursor.moveToFirst()) {
            studySetIds = "(";
            while (!cursor.isAfterLast()) {
                dueCardCount ++;
                studySetIds += cursor.getString(0) + ", ";
                cursor.moveToNext();
            }
            // drop the separator from the last part of the string
            studySetIds = studySetIds.substring(0, studySetIds.length() - 2) + 
                    ")";
            Log.d(TAG, "studySetIds: " + studySetIds);
            Log.d(TAG, "dueCardCount: " + dueCardCount);
            
        } else {
            cardMode = CARD_MODE_NONE_DUE;
        }
        cursor.close();

        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
        // get any preferences that may have changed
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }
        
        // set the typeface when the app is resumed in case it's changed
        setCardTypeface();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (cardMode == CARD_MODE_DUE) {
            return new CursorLoader(this,
                    CardProvider.CONTENT_URI,
                    PROJECTION_CARDS,
                    CardDatabaseHelper._ID + " IN " + studySetIds,
                    null,
                    CardDatabaseHelper.RANDOM);
            
        } else {
            Log.d(TAG, "Now showing new cards...");
            int initialStudySetCount = 0;
            int studySetCount = StudySetHelper.getStudySetCount(this,
                    studySetId);
            
            // get initial count of cards in study set
            Cursor cursor = getContentResolver().query(
                    StudySetProvider.CONTENT_URI_META,
                    new String[] {
                            StudySetDatabaseHelper.META_INITIAL_COUNT_DATE,
                            StudySetDatabaseHelper.META_INITIAL_COUNT},
                    StudySetDatabaseHelper._ID + " = ? ",
                    new String[] {"" + studySetId},
                    null);
            
            if (cursor.moveToFirst()) {
                initialStudySetCount = 
                    StudySetHelper.maybeUpdateInitialStudySetCount(
                            this, studySetId, studySetCount, 
                            cursor.getString(0), cursor.getInt(1));
            }
            cursor.close();
            
            /* 
             * don't show more than the max number of total or new cards
             * (minus new cards already shown today: total cards in study set
             * minus initial study set card count)
             */
            int limit = Cards.MAX_CARDS_TO_SHOW - dueCardCount;
            if (limit > Cards.MAX_NEW_CARDS_TO_SHOW - (studySetCount - 
                    initialStudySetCount)) {
                limit = Cards.MAX_NEW_CARDS_TO_SHOW - (studySetCount - 
                        initialStudySetCount);
            }
            
            Log.d(TAG, "new cards to show=" + limit);
            
            String limitString = studySetCount + "," + limit;
            
            CardQueryHelper cqh = new CardQueryHelper(this, cardGroup, 
                    cardSubgroup);
            
            return new CursorLoader(this,
                    // add the limit to the content uri
                    CardProvider.CONTENT_URI.buildUpon().appendQueryParameter(
                            CardProvider.QUERY_PARAMETER_LIMIT,
                            limitString).build(),
                    PROJECTION_CARDS,
                    cqh.getSelection(),
                    cqh.getSelectionArgs(),
                    cqh.getSortOrder()
            );
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cardsCursor = data;
        if (cardsCursor.moveToFirst()) {
            switch (cardMode) {
            case CARD_MODE_NONE_DUE:
                cardMode = CARD_MODE_NEW;
            case CARD_MODE_DUE:
                // call showFirstCard() so that no animations are shown
                showFirstCard();
                break;
            case CARD_MODE_NEW:
                // show the next card animations, but don't move the cursor 
                showNextCard(false);
                break;
            }
        } else {
            switch (cardMode) {
            case CARD_MODE_DUE:
                // we should never get here
                Log.e(TAG, "for some reason the cursor is empty...");
                break;
            case CARD_MODE_NONE_DUE:
            case CARD_MODE_NEW:
// TODO: handle if there are no cards at all to show
                // either no new cards or no cards to show at all, show the 
                // summary activity
                showSummary();
                break;
            }
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    /* Inflates the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /* Handles menu selections */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
// TODO: do we want to add a way to go back to ChooseStudySet?
/*
            case R.id.menu_choose_cards:
                chooseCardSet();
                return true;
*/
            case R.id.menu_help:
                startActivity(new Intent(this, Help.class));
                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, Preferences.class));
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
                + event);
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
            showNextCard(RESPONSE_KNOWN);
            break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
            showNextCard(RESPONSE_UNKNOWN);
            break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
            showPrevCard();
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            showNextCard(RESPONSE_IFFY);
            break;
        case KeyEvent.KEYCODE_DPAD_CENTER:
            flipCard();
            break;
        default:
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }
    
    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // if the swipe goes too far up or down, don't count it as a horizontal swipe
                if (Math.abs(e1.getY() - e2.getY()) < SWIPE_MAX_OFF_PATH) {
                    // from http://stackoverflow.com/questions/4098198/adding-fling-gesture-to-an-image-view-android
                    // right to left
                    if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        showNextCard(RESPONSE_IFFY);
                        return true;
                    // left to right
                    }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        showPrevCard();
                        return true;
                    }
                }
                // if the swipe goes too far left or right, don't count it as a vertical swipe
                if (Math.abs(e1.getX() - e2.getX()) < SWIPE_MAX_OFF_PATH) {
                    // bottom to top
                    if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        showNextCard(RESPONSE_KNOWN);
                        return true;
                    // top to bottom
                    }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        showNextCard(RESPONSE_UNKNOWN);
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                Log.e(TAG, "ERROR: Exception caught when swiping");
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp()");
            flipCard();
            return true;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent called");
        if (gestureDetector.onTouchEvent(event))
            return true;
        else
            return false;
    }
    
    private void showFirstCard() {
        setContentView(R.layout.cards);
        
        vf = (ViewFlipper)findViewById(R.id.flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, 
                R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, 
                R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, 
                R.anim.slide_right_out);
        
        gestureDetector = new GestureDetector(new MyGestureDetector());
        
        // set the typeface when the app is initially opened
        setCardTypeface();
        
        setCurrentCardText();
    }

    private void setCardTypeface() {
        /*
         * fixArabic is initialized in onResume(), and vf is initialized in 
         * showFirstCard().  the first time the app is run, onResume() will get 
         * called before showFirstCard(), so double-check that fixArabic and vf are 
         * both initialized
         */
        if (fixArabic != null && vf != null) {
            if (fixArabic) {
                // set the typeface for the TextViews within the ViewFlipper
                Typeface tf = Typeface.createFromAsset(getAssets(), 
                        Cards.ARABIC_TYPEFACE);
                ((TextView) vf.findViewById(R.id.textview1)).setTypeface(tf);
                ((TextView) vf.findViewById(R.id.textview2)).setTypeface(tf);
    
            } else {
                // reset to the default typeface
                ((TextView) vf.findViewById(R.id.textview1)).setTypeface(
                        Typeface.DEFAULT);
                ((TextView) vf.findViewById(R.id.textview2)).setTypeface(
                        Typeface.DEFAULT);
            }
        }
    }
    
    private void setCardText(int layoutIndexToShow) {
        ViewGroup rl = (RelativeLayout)vf.getChildAt(layoutIndexToShow);
        TextView tv = (TextView)rl.getChildAt(0);
        
        Log.d(TAG, "currentLang=" + currentLang);
        Log.d(TAG, "current card ID=" + cardsCursor.getString(0));
        
        if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ENGLISH)) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(cardsCursor.getString(1));
            
        } else if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ARABIC)) {
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            String arabic = cardsCursor.getString(2);
            if (fixArabic) {
                arabic = Cards.fixArabic(arabic, showVowels);
            }
            if (showVowels) {
                tv.setText(arabic);
            } else {
                tv.setText(Cards.removeVowels(arabic));
            }
        }
    }
    
    private void setCurrentCardText() {
        setCardText(vf.getDisplayedChild());
    }
    
    /*
     * sets the text of the card that isn't seen
     */
    private void setUnseenCardText() {
        // the index of the layout view we want to show
        int layoutIndexToShow;
        // we want to change the layout that isn't currently being shown
        if (vf.getDisplayedChild() == 0) {
            layoutIndexToShow = 1;
        } else {
            layoutIndexToShow = 0;
        }
        
        setCardText(layoutIndexToShow);
    }
    
    private void showNextCard(int response) {
        if (prevCardCount > 0) {
            prevCardCount --;
        } else {
            // only update the card interval if we're seeing the card for the 
            // first time this session
            updateCardInterval(cardsCursor.getString(0), response);
        }
        
        showNextCard(true);
    }
    
    /*
     * takes a boolean: whether or not to move the cursor to the next item
     */
    private void showNextCard(boolean moveCursor) {
        if (cardsCursor.isLast()) {
            // if we're out of due cards
            if (cardMode == CARD_MODE_DUE) {
                // show new cards
                cardMode = CARD_MODE_NEW;
                
                getSupportLoaderManager().restartLoader(0, null, this);
             
            } else {
                // no more new cards, show the summary activity
                showSummary();
            }
            
        } else {
            if (moveCursor) {
                cardsCursor.moveToNext();
            }
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideLeftIn);
            vf.setOutAnimation(slideLeftOut);
            vf.showNext();
        }
    }
    
    private void showPrevCard() {
        if (cardsCursor.isFirst()) {
            currentToast = Cards.showToast(this, currentToast, "No previous cards!");
        } else {
            prevCardCount ++;
            cardsCursor.moveToPrevious();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
        }
    }
    
    private void flipCard() {
        if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ENGLISH)) {
            currentLang = Cards.LANGUAGE_ARABIC;
            
        } else if (currentLang.toLowerCase().equals(Cards.LANGUAGE_ARABIC)) {
            currentLang = Cards.LANGUAGE_ENGLISH;
        }
        // update the text of the current card
        setCurrentCardText();
    }
    
    private void updateCardInterval(String cardId, int response) {
        switch(response) {
        case RESPONSE_KNOWN:
            knownCardCount ++;
            break;
        case RESPONSE_IFFY:
            iffyCardCount ++;
            break;
        case RESPONSE_UNKNOWN:
            unknownCardCount ++;
            break;
        }
        
        final String[] COLUMNS = {StudySetDatabaseHelper.SET_INTERVAL};
        final String SELECTION = StudySetDatabaseHelper.SET_CARD_ID + " = ? ";
        final String[] SELECTIONARGS = {cardId};
        
        long newDueTime;
        int newInterval;
        int oldInterval;
        
        Cursor cursor = getContentResolver().query(
                ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                studySetId),
                COLUMNS,
                SELECTION,
                SELECTIONARGS,
                null);

        if (cursor.moveToFirst()) {
            oldInterval = cursor.getInt(0);
            
            switch(response) {
            case RESPONSE_KNOWN:
                // create the new interval and round it
                newInterval = Math.round(oldInterval * Cards.MULTIPLIER_KNOWN);
                break;
            case RESPONSE_IFFY:
                newInterval = Math.round(oldInterval * Cards.MULTIPLIER_IFFY);
                break;
            case RESPONSE_UNKNOWN:
                newInterval = Math.round(oldInterval * 
                        Cards.MULTIPLIER_UNKNOWN);
                
                // don't allow the interval to go below the minimum
                if (newInterval < Cards.MIN_INTERVAL) {
                    newInterval = Cards.MIN_INTERVAL;
                // don't allow the interval to go above the maximum
                } else if (newInterval > Cards.MAX_UNKNOWN_INTERVAL) {
                    newInterval = Cards.MAX_UNKNOWN_INTERVAL;
                }
                break;
            default:
                Log.e(TAG, "ERROR: unknown response");
                newInterval = oldInterval;
            }
        } else {
            switch(response) {
            case RESPONSE_KNOWN:
                newInterval = Cards.FIRST_INTERVAL_KNOWN;
                break;
            case RESPONSE_IFFY:
                newInterval = Cards.FIRST_INTERVAL_IFFY;
                break;
            case RESPONSE_UNKNOWN:
                newInterval = Cards.MIN_INTERVAL;
                break;
            default:
                Log.e(TAG, "ERROR: unknown response");
                newInterval = Cards.MIN_INTERVAL;
            }
        }
        cursor.close();

        newDueTime = System.currentTimeMillis() + (newInterval * 
                ONE_HOUR_IN_MS);
        
        Log.d(TAG, "cardId=" + cardId);
        Log.d(TAG, "newInterval=" + newInterval);
        Log.d(TAG, "newDueTime=" + newDueTime);
        
        ContentValues cv = new ContentValues();
        cv.put(StudySetDatabaseHelper.SET_CARD_ID, cardId);
        cv.put(StudySetDatabaseHelper.SET_INTERVAL, newInterval);
        cv.put(StudySetDatabaseHelper.SET_DUE_TIME, newDueTime);
        
        // this is actually a REPLACE (INSERT OR REPLACE)
        getContentResolver().insert(
                ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                        studySetId),
                cv);
    }
    
    private void showSummary() {
// TODO: put these into string resources
        final String[] summary_pie_labels = new String[] {
                "known",
                "iffy",
                "unknown"
            };
            final int[] summary_pie_data = new int[] {
                    knownCardCount, 
                    iffyCardCount,
                    unknownCardCount
                    };
// TODO: put these into int or color resources
            int[] colors = {-10027162, -3276954, -39322};
            // color-blind safe colors
/*                int[] colors = {
                    Color.parseColor("#1BA1E2"),
                    -3276954,
                    Color.parseColor("#674f00")
                    };
*/
            Intent i = new Intent(this, ChartPanelActivity.class);
// TODO: put this into string resource
            i.putExtra(Intent.EXTRA_TITLE, "Summary");
            i.putExtra(ChartPanelActivity.EXTRA_LABELS, summary_pie_labels);
            i.putExtra(ChartPanelActivity.EXTRA_DATA, summary_pie_data);
            i.putExtra(ChartPanelActivity.EXTRA_COLORS, colors);
            startActivity(i);
            finish();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
