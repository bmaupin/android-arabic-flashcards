package us.bmaupin.flashcards.arabic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amr.arabic.ArabicUtilities;

import android.util.Log;

public class Cards {
	private static final String TAG = "Cards";
	
    static final float ARABIC_CARD_TEXT_SIZE = 56f;
    static final float ENGLISH_CARD_TEXT_SIZE = 42f;
    
    static final String ARABIC_TYPEFACE = "fonts/KacstOne.ttf";
    
	static final String EXTRA_CARD_GROUP = 
			"android.intent.extra.EXTRA_CARD_GROUP";
	static final String EXTRA_CARD_SUBGROUP = 
			"android.intent.extra.EXTRA_CARD_SUBGROUP";
	static final String EXTRA_STUDY_SET_ID = 
			"android.intent.extra.EXTRA_STUDY_SET_ID";
	static final String EXTRA_STUDY_SET_LANGUAGE = 
			"android.intent.extra.EXTRA_STUDY_SET_LANGUAGE";
	
	static final String LANGUAGE_ARABIC = "arabic";
	static final String LANGUAGE_ENGLISH = "english";
	
	static final float MULTIPLIER_KNOWN = 2.2f;
	static final float MULTIPLIER_IFFY = 1.4f;
	static final float MULTIPLIER_UNKNOWN = 0.5f;
	
	static final int FIRST_INTERVAL_KNOWN = 68;
	static final int FIRST_INTERVAL_IFFY = 20;
	// minimum interval for a card in a study set
	static final int MIN_INTERVAL = 8;
	// maximum interval for a card marked as unknown in a study set
	static final int MAX_UNKNOWN_INTERVAL = 144;
    // we'll probably replace these later using shared preferences
    // how many total cards to show per study set session
    static final int MAX_CARDS_TO_SHOW = 20;
    // max new cards to show (per day)
    static final int MAX_NEW_CARDS_TO_SHOW = 10;
    
    /**
     * apply a bunch of modifications to arabic text so it doesn't look terrible
     * @param s
     * @param showVowels
     * @return
     */
    static String fixArabic(String s, boolean showVowels) {
        // reshape the card
        s = ArabicUtilities.reshape(s);
        // this fixes issues with the final character having neutral 
        // direction (diacritics, parentheses, etc.)
        s += '\u200f';
        
//        Log.d(TAG, "UNICODE: " + splitString(s));
//        Log.d(TAG, "UNICODE: " + getUnicodeCodes(s));
        
        // only fix the sheddas if we're showing the vowels
        if (showVowels) {
            return fixSheddas(s);
        } else {
            return s;
        }
    }
    
    /**
     * Replaces certain combinations of shedda plus another haraka with custom
     * unicode characters (requiring a font customized with these characters)
     * and returns the string, since Android doesn't properly show the correct
     * ligatures for these combinations.
     * @param s
     * @return
     */
    static String fixSheddas(String s) {
        char[] charArray = s.toCharArray();
        String fixedString = "";
        boolean prevShedda = false;
        
        for (char c : charArray) {
            if (c == '\u0651') {
                prevShedda = true;
            } else {
                // the previous character was a shedda
                if (prevShedda) {
                    // reset our flag
                    prevShedda = false;
                    // fathatan
                    if (c == '\u064b') {
                        fixedString += '\ufbc2';
                    // dammatan
                    } else if (c == '\u064c') {
                        fixedString += '\ufbc3';
                    // kasratan
                    } else if (c == '\u064d') {
                        fixedString += '\ufbc4';
                    // fatha
                    } else if (c == '\u064e') {
                        fixedString += '\ufbc5';
                    // damma
                    } else if (c == '\u064f') {
                        fixedString += '\ufbc6';
                    // kasra
                    } else if (c == '\u0650') {
                        fixedString += '\ufbc7';
                    } else {
                        // add the shedda back
                        fixedString += '\u0651';
                        // add the current character
                        fixedString += c;
                    }
                } else {
                    fixedString += c;
                }
            }
        }
        
        return fixedString;
    }
    
    /**
     * Accepts a string, removes Arabic vowels from it, and returns the string
     * with the vowels removed.
     * @param s
     * @return
     */
    static String removeVowels(String s) {
        Character[] vowels = {
                '\u064e',  // fatha, short a
                '\u064b',  // double fatha
                '\u0650',  // kasra, short i
                '\u064d',  // double kasra
                '\u064f',  // damma, short u
                '\u064c',  // double damma
                '\u0652',  // sukkun, nothing
                '\u0651',  // shedda, double
        };
        List<Character> vowelList = java.util.Arrays.asList(vowels);
        
        String vowelsRemoved = "";
        for (char c : s.toCharArray()) {
            if (!vowelList.contains(c)) {
                vowelsRemoved += c;
            }
        }
        
        return vowelsRemoved;
    }
    
    static int longToInteger(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
    
    static int stringToInteger(String s) {
        try {
            int i = Integer.parseInt(s.trim());
            return i;
        } catch (NumberFormatException e) {
            Log.e(TAG, "stringToInteger: error: " + e.getMessage());
            return 0;
        }
    }
    
//******************************************* DEBUGGING ************************    

    static String getUnicodeCodes(String s) {
        char[] charArray = s.toCharArray();
        String unicodeString = "";
        
        for (char c : charArray) {
            if (!unicodeString.equals("")) {
                unicodeString += (", ");
            }
            unicodeString += String.format ("%04x", (int)c);
        }
        
        return unicodeString;
    }
    
    @SuppressWarnings("serial")
    static String splitString(String s) {
        Map<Character, String> miscCodes = new HashMap<Character, String>() {
        {
            put('\u200e', "LTR");
            put('\u200f', "RTL");
            put('\u202a', "LTRE");
            put('\u202b', "RTLE");
            put('\u202c', "POP");
            put('\u202d', "LTRO");
            put('\u202e', "RTLO");
            put(' ', "SPACE");
            put('\n', "\\n");
        }};
        
        char[] charArray = s.toCharArray();
        String unicodeString = "";
        
        for (char c : charArray) {
            if (!unicodeString.equals("")) {
                unicodeString += (", ");
            }
            if (miscCodes.containsKey(c)) {
                unicodeString += miscCodes.get(c);
            } else {
                unicodeString += c;
            }
        }
        unicodeString = '\u202d' + unicodeString + '\u202c';
        
        return unicodeString;
    }
}
