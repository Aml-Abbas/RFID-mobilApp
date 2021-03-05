package com.example.rfid_mobilapp;

import org.junit.Assert;
import org.junit.Test;


public class NfcTagUtilTest {

    @Test
    public void setBarcodeTest() {
        byte[] data= new byte[32];
        String barcode = "AA9876543219876S";
        byte[] afterSetBarcode = NfcTagUtil.setBarcode(barcode, data);
        byte[] correct = new byte[32];
        for (int i = 0; i < 16; i++) {
            correct[i + 3] = (byte) barcode.charAt(i);
        }
        Assert.assertArrayEquals(afterSetBarcode, correct);
    }
}
