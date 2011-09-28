package us.bmaupin.flashcards.arabic;

//$Id$

import java.util.List;

import android.util.Log;

public class HelperMethods {
    private static final String TAG = "HelperMethods";
    
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
    
    static int stringToInteger(String s) {
        try {
            int i = Integer.parseInt(s.trim());
            return i;
        } catch (NumberFormatException e) {
            Log.d(TAG, "stringToInteger: error: " + e.getMessage());
            return 0;
        }
    }
}
