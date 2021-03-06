package ca.bmaupin.flashcards.arabic;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
import ca.bmaupin.flashcards.arabic.data.CardQueryHelper;
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

public class BrowseCards extends BaseActivity 
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "ShowCards";
    // constants for swipe
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    
    private static final String[] PROJECTION = new String[] {
        CardDatabaseHelper.CARDS_TABLE + "." + CardDatabaseHelper._ID,
        CardDatabaseHelper.CARDS_ENGLISH,
        CardDatabaseHelper.CARDS_ARABIC,
    };
    
    private String cardGroup;
    private String cardSubgroup;
    // current card language
    private String currentLang;
    // to hold the current toast so we can update it
    Toast currentToast;
    private Cursor cursor;
    // default card language 
    private String defaultLang;
    // whether or not to apply arabic fixes
    private Boolean fixArabic;
    private GestureDetector gestureDetector;
    private SharedPreferences preferences;
    private Resources resources;
    // whether or not to show arabic vowels
    private boolean showVowels;
    private Animation slideLeftIn;
    private Animation slideLeftOut;
    private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper vf;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

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
        
        Bundle bundle = this.getIntent().getExtras(); 
        
        cardGroup = bundle.getString(Cards.EXTRA_CARD_GROUP);
        cardSubgroup = bundle.getString(Cards.EXTRA_CARD_SUBGROUP);
        
        getSupportLoaderManager().initLoader(0, null, this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        
        // get any preferences that may have changed
/*        
        ch.setAskCardOrder(preferences.getBoolean(
              getString(R.string.preferences_ask_card_order),
              resources.getBoolean(R.bool.preferences_ask_card_order_default)));
        // if we're gonna ask for card order anyway, don't need to change it
        if (!ch.isAskCardOrder()) {
            ch.setCardOrder(preferences.getString(
                   getString(R.string.preferences_default_card_order), 
                   getString(R.string.preferences_default_card_order_default)));
        }
*/
        defaultLang = preferences.getString(
                getString(R.string.preferences_default_lang), 
                getString(R.string.preferences_default_lang_default));
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
        CardQueryHelper cqh = new CardQueryHelper(this, cardGroup, 
                cardSubgroup);
        
        return new CursorLoader(this,
                cqh.getContentUri(),
                PROJECTION,
                cqh.getSelection(),
                cqh.getSelectionArgs(),
                cqh.getSortOrder()
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;

        // make sure the cursor isn't empty
        if (cursor.moveToFirst()) {
            showFirstCard();
        } else {
            currentToast = Cards.showToast(this, currentToast, "No cards to show!");
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
        Log.d(TAG, "onKeyDown: keycode=" + keyCode + ", event="
                + event);
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            showPrevCard();
            break;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            showNextCard();
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
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextCard();
                    return true;
                // left to right
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showPrevCard();
                    return true;
                }
                return false;
            } catch (Exception e) {
                // nothing
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
        
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            tv.setTextSize(Cards.ENGLISH_CARD_TEXT_SIZE);
            tv.setText(cursor.getString(1));
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
            tv.setTextSize(Cards.ARABIC_CARD_TEXT_SIZE);
            String arabic = cursor.getString(2);
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
    
    private void showNextCard() {
        if (cursor.isLast()) {
            // let people know there are no more cards...
            currentToast = Cards.showToast(this, currentToast, "No more cards!");
        } else {
            cursor.moveToNext();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideLeftIn);
            vf.setOutAnimation(slideLeftOut);
            vf.showNext();
        }
    }
    
    private void showPrevCard() {
        if (cursor.isFirst()) {
            currentToast = Cards.showToast(this, currentToast, "No previous cards!");
        } else {
            cursor.moveToPrevious();
            // reset the card language that will show first
            currentLang = defaultLang;
            setUnseenCardText();
            
            vf.setInAnimation(slideRightIn);
            vf.setOutAnimation(slideRightOut);
            vf.showPrevious();
        }
    }
    
    private void flipCard() {
        if (currentLang.equals(Cards.LANGUAGE_ENGLISH)) {
            currentLang = Cards.LANGUAGE_ARABIC;
            
        } else if (currentLang.equals(Cards.LANGUAGE_ARABIC)) {
            currentLang = Cards.LANGUAGE_ENGLISH;
        }
        // update the text of the current card
        setCurrentCardText();
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
