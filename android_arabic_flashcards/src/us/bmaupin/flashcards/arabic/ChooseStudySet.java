package us.bmaupin.flashcards.arabic;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;

public class ChooseStudySet extends ListActivity {
    private static final String TAG = "ChooseStudySet";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
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
