package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChooseCardSet extends Activity {
	//unique dialog id
	private static final int DIALOG_CARD_SET_AHLAN_WA_SAHLAN = 0;
	static final String CARD_SET = "card_set";
	static final String CARD_SUBSET = "card_subset";
	private static final String TAG = "ChooseCards";
	
	String selectedChapter;
	private DatabaseHelper helper;
	private SQLiteDatabase db; 
	
	Context context = ChooseCardSet.this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_cards);

		int layoutID = android.R.layout.simple_list_item_1;
		
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.card_set_main_items,
				layoutID);
		
		final ListView lv = (ListView)findViewById(R.id.myListView);
		
		lv.setAdapter(adapter);	

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				
				CharSequence itemText = adapter.getItem(pos);
				
                if (itemText.equals(getString(R.string.card_set_all))) {
                    Intent result = new Intent();
                    result.putExtra(CARD_SET, 
                            getString(R.string.card_set_all));
                    setResult(RESULT_OK, result);
                    finish();
                    
                } else if (itemText.equals(getString(
                        R.string.card_set_ahlan_wa_sahlan))) {
					showDialog(DIALOG_CARD_SET_AHLAN_WA_SAHLAN);
					
                } else if (itemText.equals(getString(
                        R.string.card_set_categories))) {
// TODO: implement this
//                    showDialog(DIALOG_CARD_SET_CATEGORIES);
                    
                } else if (itemText.equals(getString(
                        R.string.card_set_parts_of_speech))) {
// TODO: implement this
//                    showDialog(DIALOG_CARD_SET_PARTS_OF_SPEECH);
                    
                } else if (itemText.equals(getString(
                        R.string.card_set_unknown))) {
                    Intent result = new Intent();
                    result.putExtra(CARD_SET, 
                            getString(R.string.card_set_unknown));
                    setResult(RESULT_OK, result);
                    finish();
                }
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CARD_SET_AHLAN_WA_SAHLAN:
				return createAWSChapterDialog();
		}
		return null;
	}
	
	private Dialog createAWSChapterDialog() {
		Log.d(TAG, "createAWSChapterDialog");
		
		final String[] chapters = getChapters();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_choose_aws_chapter_title);
		builder.setItems(chapters, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
				Log.d(TAG, "createAWSChapterDialog: int=" + item);
				Log.d(TAG, "createAWSChapterDialog: chapter=" + chapters[item]);
				
				Intent result = new Intent();
				result.putExtra(CARD_SET, 
				        getString(R.string.card_set_ahlan_wa_sahlan));
				result.putExtra(CARD_SUBSET, chapters[item]);
				
				setResult(RESULT_OK, result);
				finish();
		    }
		});
		AlertDialog ad = builder.create();
		return ad;
	}
	
	private String[] getChapters() {
		Log.d(TAG, "getChapters called");
		String[] chapters;
	    
	    String[] FROM = {"aws_chapter"};
	    // as much fun as it'd be, let's not get null values
	    String WHERE = "aws_chapter not NULL";
	    
        helper = new DatabaseHelper(this);
        db = helper.getReadableDatabase();
	    
	    Cursor mCursor = db.query(true, "words", FROM, WHERE, null, null, null, null, null);
	    startManagingCursor(mCursor);
	    
	    // get the number of chapters since we're using an immutable array
	    int chapterCount = mCursor.getCount();
	    Log.d(TAG, "getChapters: chapterCount=" + chapterCount);
	    
	    chapters = new String[chapterCount];
	    int chapterIndex = 0;
	    while (mCursor.moveToNext()) {
	    	chapters[chapterIndex] = mCursor.getString(0);
	    	chapterIndex ++;   	
	    }
	    
	    // close the database connection
	    helper.close();
	    Log.d(TAG, "getChapters: returning");
	    return chapters;
	}
}
