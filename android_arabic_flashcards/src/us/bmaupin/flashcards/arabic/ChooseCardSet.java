package us.bmaupin.flashcards.arabic;

// $Id$

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
	private static final int DIALOG_CARD_SET_CATEGORIES = 1;
	private static final int DIALOG_CARD_SET_PARTS_OF_SPEECH = 2;
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
                    showDialog(DIALOG_CARD_SET_CATEGORIES);
                    
                } else if (itemText.equals(getString(
                        R.string.card_set_parts_of_speech))) {
                    showDialog(DIALOG_CARD_SET_PARTS_OF_SPEECH);
                    
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
				return createChooseAWSChapterDialog();
            case DIALOG_CARD_SET_CATEGORIES:
                return createChooseCategoryDialog();
            case DIALOG_CARD_SET_PARTS_OF_SPEECH:
                return createChoosePartOfSpeechDialog();
		}
		return null;
	}
	
	private Dialog createChooseAWSChapterDialog() {
		Log.d(TAG, "createAWSChapterDialog");
		
		final String[] chapters = getColumnValues(
		        DatabaseHelper.AWS_CHAPTERS_TABLE, 
		        DatabaseHelper.AWS_CHAPTERS_CHAPTER);
		
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
	
    private Dialog createChooseCategoryDialog() {
        Log.d(TAG, "createChooseCategoryDialog");
        
        final String[] categories = getColumnValues(DatabaseHelper.WORDS_TABLE, 
                DatabaseHelper.WORDS_CATEGORY);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_card_category_title);
        builder.setItems(categories, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.d(TAG, "createChooseCategoryDialog: int=" + item);
                Log.d(TAG, "createChooseCategoryDialog: category=" + 
                        categories[item]);
                
                Intent result = new Intent();
                result.putExtra(CARD_SET, 
                        getString(R.string.card_set_categories));
                result.putExtra(CARD_SUBSET, categories[item]);
                
                setResult(RESULT_OK, result);
                finish();
            }
        });
        AlertDialog ad = builder.create();
        return ad;
    }
    
    private Dialog createChoosePartOfSpeechDialog() {
        Log.d(TAG, "createChoosePartOfSpeechDialog");
        
        Resources res = getResources();
        final String[] partsOfSpeechEntries = res.getStringArray(
                R.array.dialog_parts_of_speech_entries);
        final String[] partsOfSpeechValues = res.getStringArray(
                R.array.dialog_parts_of_speech_values);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_part_of_speech_title);
        builder.setItems(partsOfSpeechEntries, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.d(TAG, "createChoosePartOfSpeechDialog: int=" + item);
                Log.d(TAG, "createChoosePartOfSpeechDialog: partOfSpeech=" + 
                        partsOfSpeechEntries[item]);
                
                Intent result = new Intent();
                result.putExtra(CARD_SET, 
                        getString(R.string.card_set_parts_of_speech));
                // make sure we put the part of speech value and not the entry
                result.putExtra(CARD_SUBSET, partsOfSpeechValues[item]);
                
                setResult(RESULT_OK, result);
                finish();
            }
        });
        AlertDialog ad = builder.create();
        return ad;
    }
    
    private String[] getColumnValues(String table, String column) {
        Log.d(TAG, "getColumnValues called");
        String[] columnValues;
        
        String[] FROM = {column};
        // as much fun as it'd be, let's not get null or empty values
        String WHERE = column + " not NULL and " + column + " != ''";
        
        helper = new DatabaseHelper(this);
        db = helper.getReadableDatabase();
        
        // do a query for unique/distinct rows only
        Cursor mCursor = db.query(true, table, FROM, WHERE, null, null, null, 
                null, null);
        startManagingCursor(mCursor);
        
        // get the number of columns since we're using an immutable array
        int columnCount = mCursor.getCount();
        Log.d(TAG, "getColumnValues: columnCount=" + columnCount);
        
        columnValues = new String[columnCount];
        int columnIndex = 0;
        while (mCursor.moveToNext()) {
            columnValues[columnIndex] = mCursor.getString(0);
            columnIndex ++;    
        }
        
        // close the database connection
        helper.close();
        Log.d(TAG, "getColumnValues: returning");
        return columnValues;
    }
}
