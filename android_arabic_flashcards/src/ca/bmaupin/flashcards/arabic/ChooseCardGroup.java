package ca.bmaupin.flashcards.arabic;

import ca.bmaupin.flashcards.arabic.data.CardDatabaseHelper;
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

public class ChooseCardGroup extends Activity {
	//unique dialog id
	private static final int DIALOG_CARD_GROUP_AHLAN_WA_SAHLAN = 0;
	private static final int DIALOG_CARD_GROUP_CATEGORIES = 1;
	private static final int DIALOG_CARD_GROUP_PARTS_OF_SPEECH = 2;
	private static final String TAG = "ChooseCardGroup";
	
	String selectedChapter;
	private CardDatabaseHelper helper;
	private SQLiteDatabase db; 
	
	Context context = ChooseCardGroup.this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_cards);

		int layoutID = android.R.layout.simple_list_item_1;
		
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.card_group_main_items,
				layoutID);
		
		final ListView lv = (ListView)findViewById(R.id.myListView);
		
		lv.setAdapter(adapter);	

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				
				CharSequence itemText = adapter.getItem(pos);
				
                if (itemText.equals(getString(R.string.card_group_all))) {
                    Intent result = new Intent();
                    result.putExtra(Cards.EXTRA_CARD_GROUP, 
                            getString(R.string.card_group_all));
                    setResult(RESULT_OK, result);
                    finish();
                    
                } else if (itemText.equals(getString(
                        R.string.card_group_ahlan_wa_sahlan))) {
					showDialog(DIALOG_CARD_GROUP_AHLAN_WA_SAHLAN);
					
                } else if (itemText.equals(getString(
                        R.string.card_group_categories))) {
                    showDialog(DIALOG_CARD_GROUP_CATEGORIES);
                    
                } else if (itemText.equals(getString(
                        R.string.card_group_parts_of_speech))) {
                    showDialog(DIALOG_CARD_GROUP_PARTS_OF_SPEECH);
                    
                }
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_CARD_GROUP_AHLAN_WA_SAHLAN:
				return createChooseAWSChapterDialog();
            case DIALOG_CARD_GROUP_CATEGORIES:
                return createChooseCategoryDialog();
            case DIALOG_CARD_GROUP_PARTS_OF_SPEECH:
                return createChoosePartOfSpeechDialog();
		}
		return null;
	}
	
	private Dialog createChooseAWSChapterDialog() {
		Log.d(TAG, "createAWSChapterDialog");
		
		final String[] chapters = getColumnValues(
		        CardDatabaseHelper.AWS_CHAPTERS_TABLE, 
		        CardDatabaseHelper.AWS_CHAPTERS_CHAPTER);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_choose_aws_chapter_title);
		builder.setItems(chapters, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
				Log.d(TAG, "createAWSChapterDialog: int=" + item);
				Log.d(TAG, "createAWSChapterDialog: chapter=" + chapters[item]);
				
				Intent result = new Intent();
				result.putExtra(Cards.EXTRA_CARD_GROUP, 
				        getString(R.string.card_group_ahlan_wa_sahlan));
				result.putExtra(Cards.EXTRA_CARD_SUBGROUP, chapters[item]);
				
				setResult(RESULT_OK, result);
				finish();
		    }
		});
		AlertDialog ad = builder.create();
		return ad;
	}
	
    private Dialog createChooseCategoryDialog() {
        Log.d(TAG, "createChooseCategoryDialog");
        
        final String[] categories = getColumnValues(CardDatabaseHelper.CARDS_TABLE, 
                CardDatabaseHelper.CARDS_CATEGORY);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_choose_card_category_title);
        builder.setItems(categories, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Log.d(TAG, "createChooseCategoryDialog: int=" + item);
                Log.d(TAG, "createChooseCategoryDialog: category=" + 
                        categories[item]);
                
                Intent result = new Intent();
                result.putExtra(Cards.EXTRA_CARD_GROUP, 
                        getString(R.string.card_group_categories));
                result.putExtra(Cards.EXTRA_CARD_SUBGROUP, categories[item]);
                
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
                result.putExtra(Cards.EXTRA_CARD_GROUP, 
                        getString(R.string.card_group_parts_of_speech));
                // make sure we put the part of speech value and not the entry
                result.putExtra(Cards.EXTRA_CARD_SUBGROUP, partsOfSpeechValues[item]);
                
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
        
        helper = new CardDatabaseHelper(this);
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
