package us.bmaupin.flashcards.arabic;

// $Id$

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.amr.arabic.ArabicUtilities;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

public class HelperMethods {
    private static final String TAG = "HelperMethods";
    
    /**
     * apply a bunch of modifications to arabic text so it doesn't look terrible
     * @param s
     * @param showVowels
     * @return
     */
    static String fixArabic(String s, boolean showVowels) {
        // reshape the word
        s = ArabicUtilities.reshape(s);
        // this fixes issues with the final character having neutral 
        // direction (diacritics, parentheses, etc.)
        s += '\u200f';
        
        Log.d(TAG, "UNICODE: " + splitString(s));
        Log.d(TAG, "UNICODE: " + getUnicodeCodes(s));
        
        if (showVowels) {
            return HelperMethods.fixSheddas(s);
        } else {
            return HelperMethods.removeVowels(s);
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
    
    /**
     * An attempt at eliminating redundancy and making sure we use the same 
     * typeface everywhere.
     * @param context
     * @param tv
     */
    static void setArabicTypeface(Context context, TextView tv) {
        Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/KacstOne.ttf");
        tv.setTypeface(tf);
    }
    
    static int stringToInteger(String s) {
        try {
            int i = Integer.parseInt(s.trim());
            return i;
        } catch (NumberFormatException e) {
            Log.d(TAG, "stringToInteger: error: " + e.getMessage());
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
    
    static String splitString(String s) {
        Map<Character, String> miscCodes = new HashMap<Character, String>() {{
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
