package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FreeMode extends Activity {
    private static final String TAG = "ShowCards";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.main);
        
        Bundle bundle = this.getIntent().getExtras();
        String cardSet = bundle.getString(ChooseCardSet.EXTRA_CARD_SET);
        String cardSubSet = bundle.getString(ChooseCardSet.EXTRA_CARD_SUBSET);
        
        Toast.makeText(getApplicationContext(), cardSet, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), cardSubSet, Toast.LENGTH_SHORT).show();
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
