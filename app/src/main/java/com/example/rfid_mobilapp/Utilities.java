package com.example.rfid_mobilapp;

public class Utilities {

    public static char[] replaceStringAt(String stringValue, int start, int len, char[] currentData) {
        for (int i = 0; i < len; i++) {
            if (i >= stringValue.length()) {
                currentData[start+i]='\0';
            } else {
                currentData[start+i]=stringValue.charAt(i);
            }
        }
        return currentData;
    }
}
