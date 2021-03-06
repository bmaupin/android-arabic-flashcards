package ca.bmaupin.flashcards.arabic.data;

import android.content.Context;
import android.net.Uri;
import ca.bmaupin.flashcards.arabic.R;

public class CardQueryHelper {
    private Uri contentUri = CardProvider.CONTENT_URI;
    private String selection = "";
    private String[] selectionArgs = new String[] {};
    private String sortOrder = "";
    
    public Uri getContentUri() {
        return contentUri;
    }
    
    public String getSelection() {
        return selection;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public CardQueryHelper(Context context, String cardGroup, String cardSubgroup) {
        if (cardGroup.equals(context.getString(
                R.string.card_group_ahlan_wa_sahlan))) {
            
            // if we want to get all aws cards
            if (cardSubgroup.equals(context.getString(R.string.card_group_all_chapters))) {
                /*
                 * this looks like:
                 * select * from cards where cards._id in (select distinct card_ID from aws_chapters);
                 */
                selection = CardDatabaseHelper.CARDS_TABLE + "." + 
                        CardDatabaseHelper._ID + " IN (SELECT DISTINCT " +
                        CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " FROM " + 
                        CardDatabaseHelper.AWS_CHAPTERS_TABLE + ") ";
                // make sure we don't do a join in the query
                contentUri = contentUri.buildUpon().appendQueryParameter(
                        CardProvider.QUERY_PARAMETER_JOIN,
                        CardProvider.QUERY_PARAMETER_VALUE_FALSE).build();
            
            // otherwise, get cards for a specific aws chapter
            } else {
                /*
                 * this looks like:
                 * where cards._id in (select card_ID from aws_chapters where chapter = ?) and chapter = ?
                 * 
                 * it may seem terribly redundant, but it's much faster than:
                 * where chapter = ?
                 * 
                 * because it doesn't do the left join (in the provider class) 
                 * on both entire tables
                 * 
                 * the entire query ends up looking like this:
                 * select * from cards left join aws_chapters on cards._id = aws_chapters.card_id where cards._id in (select card_ID from aws_chapters where chapter = 4) and chapter = 4 order by aws_chapters._id;
                 * 
                 * instead of this:
                 * select * from cards left join aws_chapters on cards._id = aws_chapters.card_id where chapter = 4 order by aws_chapters._id;
                 */
                selection = CardDatabaseHelper.CARDS_TABLE + "." + 
                        CardDatabaseHelper._ID + " IN (SELECT " + 
                        CardDatabaseHelper.AWS_CHAPTERS_CARD_ID + " FROM " + 
                        CardDatabaseHelper.AWS_CHAPTERS_TABLE + " WHERE " + 
                        CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = ?) AND " + 
                        CardDatabaseHelper.AWS_CHAPTERS_CHAPTER + " = ? ";
                selectionArgs = new String[] {cardSubgroup, cardSubgroup};
                sortOrder = CardDatabaseHelper.AWS_CHAPTERS_TABLE + "." + 
                        CardDatabaseHelper._ID;
            }
            
        } else if (cardGroup.equals(context.getString(
                R.string.card_group_categories))) {
            selection = CardDatabaseHelper.CARDS_CATEGORY + " = ? ";
            selectionArgs = new String[] {cardSubgroup};
            
        } else if (cardGroup.equals(context.getString(
                R.string.card_group_parts_of_speech))) {
            selection = CardDatabaseHelper.CARDS_PART + " = ? ";
            selectionArgs = new String[] {cardSubgroup};
        }
    }
}
