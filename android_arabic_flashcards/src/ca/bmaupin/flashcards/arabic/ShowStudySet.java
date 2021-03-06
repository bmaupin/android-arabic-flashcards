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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ShowStudySet extends BaseActivity 
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ShowStudySet";
    private static final int RESPONSE_KNOWN = 0;
    private static final int RESPONSE_IFFY = 1;
    private static final int RESPONSE_UNKNOWN = 2;
    // keys for items to store in bundles
    private static final String KEY_CARDS_SHOWN = "cardsShown";
    private static final String KEY_IFFY_CARD_COUNT = "iffyCardCount";
    private static final String KEY_KNOWN_CARD_COUNT = "knownCardCount";
    private static final String KEY_UNKNOWN_CARD_COUNT = "unknownCardCount";
    
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
    
    private String cardGroup;
    // number of cards shown, incremented after the card is shown
    private int cardsShown = 0;
    private String cardSubgroup;
    // max number of cards to show per session
    private int maxDueCards = 0;
    private Cursor cardsCursor;
    // current card language
    private String currentLang;
    // to hold the current toast so we can update it
    Toast currentToast;
    // default card language 
    private String defaultLang;
    // whether or not to apply arabic fixes
    private Boolean fixArabic;
    private GestureDetector gestureDetector;
    private int iffyCardCount = 0;
    private int knownCardCount = 0;
    // max number of new cards to show per day per study set
    private int maxNewCards = 0;
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
        
        // if the activity was in the background and killed in the middle of a 
        // study session, get our previous counts so we can resume the session
        if (savedInstanceState != null) {
            cardsShown = savedInstanceState.getInt(KEY_CARDS_SHOWN);
            knownCardCount = savedInstanceState.getInt(KEY_KNOWN_CARD_COUNT);
            iffyCardCount = savedInstanceState.getInt(KEY_IFFY_CARD_COUNT);
            unknownCardCount = savedInstanceState.getInt(
                    KEY_UNKNOWN_CARD_COUNT);
        }
        
        // create objects for shared preferences and resources
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        resources = getResources();
        
        // get initial values of preferences
        fixArabic = preferences.getBoolean(
                getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default));
        showVowels = preferences.getBoolean(
                getString(R.string.preferences_show_vowels),
                resources.getBoolean(R.bool.preferences_show_vowels_default));
        
        // get these values in onCreate and don't change them until the activity
        // is created again.  that will hopefully prevent things from getting 
        // screwy if these preferences are changed in the middle of a study set
        maxDueCards = Cards.stringToInteger(preferences.getString(
                getString(R.string.preferences_max_due_cards),
                resources.getString(
                        R.integer.preferences_max_due_cards_default)));
        maxNewCards = Cards.stringToInteger(preferences.getString(
                getString(R.string.preferences_max_new_cards),
                resources.getString(
                        R.integer.preferences_max_new_cards_default)));
        
        Bundle bundle = this.getIntent().getExtras();
        studySetId = bundle.getInt(Cards.EXTRA_STUDY_SET_ID);

        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
// TODO        
        Log.d(TAG, "knownCardCount=" + knownCardCount);
        Log.d(TAG, "iffyCardCount=" + iffyCardCount);
        Log.d(TAG, "unknownCardCount=" + unknownCardCount);
        
        // make sure currentLang gets set right away
        if (currentLang == null || currentLang.equals("")) {
            currentLang = defaultLang;
        }
        
        // if the fixArabic preference has changed
        if (preferences.getBoolean(getString(R.string.preferences_fix_arabic),
                resources.getBoolean(R.bool.preferences_fix_arabic_default))
                != fixArabic) {
            // update it
            fixArabic = preferences.getBoolean(
                    getString(R.string.preferences_fix_arabic),
                    resources.getBoolean(R.bool.preferences_fix_arabic_default));
            // set the typeface when the app is resumed in case it's changed
            setCardTypeface();
            // make sure the first card has been shown (vf is initialized in
            // showFirstCard())
            if (vf != null) {
                // and refresh the current card text
                setCurrentCardText();
            }
        
        // since we change the current card text either way, we only need to 
        // check if showVowels has changed if fixArabic hasn't changed
        } else {
            // if the showVowels preference has changed
            if (preferences.getBoolean(getString(R.string.preferences_show_vowels),
                    resources.getBoolean(R.bool.preferences_show_vowels_default))
                    != showVowels) {
                // update it
                showVowels = preferences.getBoolean(
                        getString(R.string.preferences_show_vowels),
                        resources.getBoolean(R.bool.preferences_show_vowels_default));
                // make sure the first card has been shown (vf is initialized in
                // showFirstCard())
                if (vf != null) {
                    // and refresh the current card text
                    setCurrentCardText();
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // get the card group, subgroup for the study set so we can figure out
        // how many cards are due
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
        
        int limit = maxDueCards - cardsShown;
        
        String selection = StudySetDatabaseHelper.SET_DUE_TIME + " < " + 
                System.currentTimeMillis();
        Log.d(TAG, "System.currentTimeMillis(): " + System.currentTimeMillis());
        
        // see if there are any cards due
        cursor = getContentResolver().query(
                // specify the study set ID and a limit
                ContentUris.withAppendedId(StudySetProvider.CONTENT_URI,
                        studySetId).buildUpon().appendQueryParameter(
                        StudySetProvider.QUERY_PARAMETER_LIMIT,
                        "" + limit).build(),
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

        // if there are due cards to show
        if (cursor.moveToFirst()) {
            studySetIds = "(";
            while (!cursor.isAfterLast()) {
                studySetIds += cursor.getString(0) + ", ";
                cursor.moveToNext();
            }
            // drop the separator from the last part of the string
            studySetIds = studySetIds.substring(0, studySetIds.length() - 2) + 
                    ")";
            Log.d(TAG, "studySetIds: " + studySetIds);
            Log.d(TAG, "dueCardCount: " + cursor.getCount());
            
            return new CursorLoader(this,
                    CardProvider.CONTENT_URI,
                    PROJECTION_CARDS,
                    CardDatabaseHelper._ID + " IN " + studySetIds,
                    null,
                    CardDatabaseHelper.RANDOM);
        }
        cursor.close();

        // if there are no due cards, show new cards
        limit = maxNewCards - cardsShown;
        
        // skip all the cards already shown to show new ones
        String limitString = StudySetHelper.getStudySetCount(this,
                studySetId) + "," + limit;
        
        CardQueryHelper cqh = new CardQueryHelper(this, cardGroup, 
                cardSubgroup);
        
        return new CursorLoader(this,
                // add the limit to the content uri
                cqh.getContentUri().buildUpon().appendQueryParameter(
                        CardProvider.QUERY_PARAMETER_LIMIT,
                        limitString).build(),
                PROJECTION_CARDS,
                cqh.getSelection(),
                cqh.getSelectionArgs(),
                cqh.getSortOrder()
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cardsCursor = data;
        
        if (cardsCursor.moveToFirst()) {
            showFirstCard();
        } else {
            Toast.makeText(getApplicationContext(), 
                    getString(R.string.show_study_set_no_cards_due), 
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event=" + event);
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
        
        if (cardsCursor.isLast()) {
            // no more cards, show the summary activity
            showSummary();
            
        } else {
            // update the next card's text
            cardsCursor.moveToNext();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            // show the sliding animation
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
        // regardless, increment the count of cards shown
        cardsShown ++;
// TODO
        Log.d(TAG, "cardsShown=" + cardsShown);
        Log.d(TAG, "knownCardCount=" + knownCardCount);
        Log.d(TAG, "iffyCardCount=" + iffyCardCount);
        Log.d(TAG, "unknownCardCount=" + unknownCardCount);
        
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
        int [] colors = new int[3];
        
        final String[] summary_pie_labels = new String[] {
            getString(R.string.chart_label_known),
            getString(R.string.chart_label_iffy),
            getString(R.string.chart_label_unknown)
        };
        final int[] summary_pie_data = new int[] {
            knownCardCount,
            iffyCardCount,
            unknownCardCount
            };
        // use color blind friendly colors if specified in preferences
        if (preferences.getBoolean(getString(
                R.string.preferences_use_color_blind_colors), 
                resources.getBoolean(
                R.bool.preferences_use_color_blind_colors_default))) {
            colors[0] = resources.getColor(R.color.chart_blue);
            colors[1] = resources.getColor(R.color.chart_yellow);
            colors[2] = resources.getColor(R.color.chart_brown);
        } else {
            colors[0] = resources.getColor(R.color.chart_green);
            colors[1] = resources.getColor(R.color.chart_yellow);
            colors[2] = resources.getColor(R.color.chart_red);
        }

        Intent i = new Intent(this, ChartPanelActivity.class);
        i.putExtra(Intent.EXTRA_TITLE, getString(R.string.chart_title));
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
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState()");
        
        // save any necessary values in case the activity gets sent to the 
        // background and killed in the middle of a study set session
        savedInstanceState.putInt(KEY_CARDS_SHOWN, cardsShown);
        savedInstanceState.putInt(KEY_KNOWN_CARD_COUNT, knownCardCount);
        savedInstanceState.putInt(KEY_IFFY_CARD_COUNT, iffyCardCount);
        savedInstanceState.putInt(KEY_UNKNOWN_CARD_COUNT, unknownCardCount);
        
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
