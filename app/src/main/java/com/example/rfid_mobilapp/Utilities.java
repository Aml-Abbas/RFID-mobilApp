package com.example.rfid_mobilapp;

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

    public static byte[] replaceByteAt(String barcode, int start, int len, byte[] currentData) {

        byte[] temp = new byte[16];
        for (int i = 0; i < barcode.length(); i++) {
            temp[i] = (byte) barcode.charAt(i);
        }

        for (int i = 0; i < len; i++) {
            currentData[start + i] = temp[i];
        }
        return currentData;
    }

    public static long calculateCRC16(final byte[] data) {
        long crc = 0xFFFF;

        for (int j = 0; j < data.length; j++) {
            crc = ((crc >>> 8) | (crc << 8)) & 0xffff;
            crc ^= (data[j] & 0xff);
            crc ^= ((crc & 0xff) >> 4);
            crc ^= (crc << 12) & 0xffff;
            crc ^= ((crc & 0xFF) << 5) & 0xffff;
        }
        crc &= 0xffff;
        return crc;

    }

    public static byte[] getDataWithoutCRC(final byte[] data) {
        byte[] dataWithoutCRC = new byte[32];
        for (int i = 0; i < 19; i++) {
            dataWithoutCRC[i] = data[i];
        }
        for (int i = 19; i < data.length; i++) {
            dataWithoutCRC[i] = data[i + 2];
        }

        return dataWithoutCRC;
    }
}