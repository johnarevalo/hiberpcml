/*
 * The MIT License
 *
 * Copyright 2011 John Arevalo <johnarevalo@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */package com.googlecode.hiberpcml;

/**
 *
 * @author John Arevalo <johnarevalo@gmail.com>
 */
public final class Util {

	private Util() {}
	
    /**
     * <p>Fills a string with the given char. The repeated char is concatenated at left side.
     * if <code>length</code> is less than <code>string</code> length
     * the string value is returned without changes.</p>
     * @param string String to complete
     * @param complete char which fills the blank spaces
     * @param length the new String length
     * @return modified string
     */
    public static String completeWith(String string, String complete, int length) {
        if (string != null && string.length() < length) {
            return repeat(complete, length - string.length()) + string;
        } else {
            return string;
        }
    }

    public static String repeat(String src, int repeat) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            buf.append(src);
        }
        return buf.toString();
    }

    public static String toCamelCase(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder camelCased = new StringBuilder("");
        String[] words = value.split(" ");
        for (String word : words) {
            String firstChar = word.substring(0, 1);
            camelCased.append(word.replaceFirst(".", firstChar.toUpperCase()));
        }

        return camelCased.toString();

    }
    
    /**
     * check whether a String is empty or not
     * @return
     */
    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
}
