package com.axiell_exjobb.RFID_mobilapp;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {

    public static void copyByteArray(byte[] fromArray, int fromIndex,
                                     byte[] toArray, int fromIndexTo, int length) {

        for (int i = 0; i < length; i++) {
            toArray[fromIndexTo + i] = fromArray[i + fromIndex];
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

    public static byte[] replaceBarcode(String barcode, int start, int len, byte[] currentData) {

        byte[] temp = new byte[16];
        for (int i = 0; i < barcode.length(); i++) {
            temp[i] = (byte) barcode.charAt(i);
        }
        for (int i = 0; i < len; i++) {
            currentData[start + i] = temp[i];
        }
        return currentData;
    }

    public static int calculateCRC16(final byte[] data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : data) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit)
                    crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return crc;

    }

    public static byte[] getDataWithoutCRC(final byte[] data) {
        byte[] dataWithoutCRC = new byte[32];
        for (int i = 0; i < 19; i++) {
            dataWithoutCRC[i] = data[i];
        }
        for (int i = 21; i < 32; i++) {
            dataWithoutCRC[i - 2] = data[i];
        }

        return dataWithoutCRC;
    }

    public static byte[] replaceCRC(int CRC, byte[] currentData) {
        currentData[19] = (byte) ((CRC >> 8) & 0xFF);
        currentData[20] = (byte) (CRC & 0xFF);
        return currentData;
    }

    public static JSONObject stringToJson(String jsonString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException err) {
            Log.d("Error", err.toString());
        }
        return jsonObject;
    }

    public static String getItemFromJson(JSONObject obj, String item) throws JSONException {
        return obj.getString(item);
    }

    public static String createJsonString(String done, String value){
        return "{\"Done\": \""+done+"\", \"value\": \""+value+"\"}";
    }
}