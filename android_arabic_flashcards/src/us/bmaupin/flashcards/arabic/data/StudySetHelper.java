package us.bmaupin.flashcards.arabic.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class StudySetHelper {
    private static final String TAG = "StudySetHelper";
    
    public static int getInitialStudySetCount(Context context, int studySetId, 
            int studySetCount) {
        int initialStudySetCount = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                StudySetDatabaseHelper.META_NEW_CARDS_DATE_FORMAT);
        
        // get initial count of cards in study set
        Cursor cursor = context.getContentResolver().query(
                StudySetProvider.CONTENT_URI_META,
                new String[] {
                        StudySetDatabaseHelper.META_INITIAL_COUNT_DATE,
                        StudySetDatabaseHelper.META_INITIAL_COUNT},
                StudySetDatabaseHelper._ID + " = ? ",
                new String[] {"" + studySetId},
                null);
        
        if (cursor.moveToFirst()) {
            String today = simpleDateFormat.format(new Date()).toString();
            
            // if the date found in the database is today
            if (cursor.getString(0).equals(today)) {
                // set the initial count of cards in the study set for today
                initialStudySetCount = cursor.getInt(1);
                Log.d(TAG, "initialStudySetCount (from db)=" + initialStudySetCount);
                
            // otherwise update the initial study set card count in the db
            } else {
                ContentValues cv = new ContentValues();
                cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT_DATE, 
                        today);
                cv.put(StudySetDatabaseHelper.META_INITIAL_COUNT, 
                        studySetCount);
                
                context.getContentResolver().update(
                        StudySetProvider.CONTENT_URI_META,
                        cv,
                        StudySetDatabaseHelper._ID + " = ? ",
                        new String[] {"" + studySetId});
                initialStudySetCount = studySetCount;
                Log.d(TAG, "initialStudySetCount=studySetCount=" + initialStudySetCount);
            }
        }
        cursor.close();
        
        return initialStudySetCount;
    }
}
