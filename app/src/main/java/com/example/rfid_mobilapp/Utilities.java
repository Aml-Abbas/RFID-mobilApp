package com.example.rfid_mobilapp;

public class Utilities {

    public static char[] initdata(byte[] in) {

        char[] userdata = new char[in.length];

        for (int i = 0; i < in.length; i++) {
            userdata[i]=(char)in[i];
        }
        return userdata;
    }

    public static void copyByteArray(byte[] fromArray, int fromIndex,
                                      byte[] toArray, int fromIndexTo, int length) {

        for (int i = 0 ; i < length; i++) {
            toArray[fromIndexTo+i] = fromArray[i + fromIndex];
        }
    }

    public static boolean isEmpty(byte[] primeItemId) {
        for (int i = 0; i < primeItemId.length; i++) {
            if (primeItemId[i] != 0) {
                return false;
            }
        }
        return true;
    }

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
