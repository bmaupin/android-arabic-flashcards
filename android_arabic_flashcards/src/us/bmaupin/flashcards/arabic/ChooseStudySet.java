package us.bmaupin.flashcards.arabic;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChooseStudySet extends ListActivity {
    private static final int REQUEST_CARD_SET = 0;
    private static final String TAG = "ChooseStudySet";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.choose_study_set);
        
        Button button = (Button)findViewById(R.id.study_set_button_free_mode);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(ChooseStudySet.this, ChooseCardSet.class);
                startActivityForResult(intent, REQUEST_CARD_SET);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        Log.d(TAG, "onActivityResult()");
        
        switch(requestCode) {
            case (REQUEST_CARD_SET) :
                if (resultCode == Activity.RESULT_OK) {
                    String cardSet = data.getStringExtra(ChooseCardSet.EXTRA_CARD_SET);
                    Log.d(TAG, "onActivityResult: cardSet=" + cardSet);
                    
                    if (data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET) == null) {
//                        ch.loadCardSet(cardSet);
                    } else {
//                        ch.loadCardSet(cardSet, data.getStringExtra(
//                                ChooseCardSet.CARD_SUBSET));
                    }
                    
//                    if (ch.isAskCardOrder()) {
//                        showDialog(DIALOG_SELECT_CARD_ORDER);
//                    } else {
//                        showFirstCard();
//                    }
                    
                    Intent intent = new Intent(this, FreeMode.class);
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SET));
                    intent.putExtra(ChooseCardSet.EXTRA_CARD_SUBSET, 
                            data.getStringExtra(ChooseCardSet.EXTRA_CARD_SUBSET));
                    startActivity(intent);
                    
//                    startActivity(new Intent(this, FreeMode.class));
                }
                break;
        }
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
