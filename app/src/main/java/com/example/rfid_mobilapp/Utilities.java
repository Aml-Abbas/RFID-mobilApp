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
        for (int i = 19; i < data.length; i++) {
            dataWithoutCRC[i] = data[i + 2];
        }

        return dataWithoutCRC;
    }
}