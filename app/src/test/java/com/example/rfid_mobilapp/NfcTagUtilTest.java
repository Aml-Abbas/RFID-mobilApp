package com.example.rfid_mobilapp;

import org.junit.Assert;
import org.junit.Test;


public class NfcTagUtilTest {

    @Test
    public void setBarcodeTest() {
        byte[] data = new byte[32];
        String barcode = "AA9876543219876S";
        byte[] afterSetBarcode = NfcTagUtil.setBarcode(barcode, data);
        byte[] correct = new byte[32];
        for (int i = 0; i < 16; i++) {
            correct[i + 3] = (byte) barcode.charAt(i);
        }
        Assert.assertArrayEquals(afterSetBarcode, correct);
    }

    @Test
    public void setCRCTest() {
        byte[] data = new byte[32];
        String barcode = "AA987654321876LL";
        byte[] afterSetBarcode = NfcTagUtil.setBarcode(barcode, data);
        byte[] afterSetCRC = NfcTagUtil.setCRC(Utilities.calculateCRC16(afterSetBarcode), data);
        byte[] correct = new byte[32];
        for (int i = 0; i < 16; i++) {
            correct[i + 3] = (byte) barcode.charAt(i);
        }
        correct[19] = -5;
        correct[20] = -40;
        Assert.assertArrayEquals(afterSetCRC, correct);
    }
}
