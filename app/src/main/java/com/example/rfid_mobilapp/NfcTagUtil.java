package com.example.rfid_mobilapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NfcTagUtil {

    private static final byte flag = (byte) 0x20;

    private static final byte writeAFICommand = (byte) 0x27;
    private static final byte writeSingleBlockCommand = (byte) 0x21;
    private static final byte readSingleBlockCommand = (byte) 0x20;
    private static final byte getSystemInfoCommand = (byte) 0x2B;

    private static final byte checkInValue = (byte) 0x07;
    private static final byte checkOutValue = (byte) 0xC2;

    public static String getItemId(Intent intent, Activity activity) {
        String payloadString = "test";

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            byte[] tagId = tag.getId();

            byte[] oldData = readBlocks(tagId, nfcV, activity, 0, 8);

            byte[] primeItemId = new byte[16];
            boolean alternativeItemId = false;

            byte[] primeItemId2 = new byte[16];
            Utilities.copyByteArray(oldData, 2, primeItemId, 0, 16);
            if (Utilities.isEmpty(primeItemId)) {
                return "No Id";
            }
            String stringOfPrimaryId = new String(primeItemId, StandardCharsets.UTF_8);

            if (stringOfPrimaryId.charAt(0) == '1') {
                alternativeItemId = true;
                byte[] OptionalBlock = readBlocks(tagId, nfcV, activity, 8, 6);
                Utilities.copyByteArray(OptionalBlock, 4, primeItemId2, 0, 16);
            }
            if (!alternativeItemId) {
                payloadString = new String(primeItemId, StandardCharsets.UTF_8);
            } else {
                payloadString = new String(primeItemId, StandardCharsets.UTF_8) +
                        new String(primeItemId2, StandardCharsets.UTF_8);
            }

        }
        return payloadString;
    }

    private static byte[] readBlocks(byte[] tagId, NfcV nfcV, Activity activity, int offset, int blocks) {
        byte[] resultData = new byte[4 * blocks];
        try {
            nfcV.connect();
            byte[] cmdRead = getCommand(tagId, readSingleBlockCommand, (byte) offset);
            for (int i = 0; i < blocks; i++) {
                cmdRead[10] = (byte) ((offset + i) & 0x0ff);
                byte[] response = nfcV.transceive(cmdRead);
                Utilities.copyByteArray(response, 1, resultData, i * 4, 4);
            }
            nfcV.close();
        } catch (IOException ioException) {
            Toast.makeText(activity, "Failed to read the tag", Toast.LENGTH_LONG).show();
        }
        return resultData;
    }

    public static void writeNewItemId(String itemId, Intent intent, Activity activity) {
        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);
            byte[] tagId = tag.getId();

            byte[] oldData = readBlocks(tagId, nfcV, activity, 0, 8);
            ;

            char[] newData = Utilities.initdata(oldData);
            char[] newDataWithBarcode = NfcTagUtil.setBarcode(itemId, newData);
            byte[] newDataToWrite = new String(newDataWithBarcode).getBytes(StandardCharsets.UTF_8);

            writeBlocks(tagId, nfcV, activity, 0, 8, newDataToWrite);
        }
    }

    private static void writeBlocks(byte[] tagId, NfcV nfcV, Activity activity, int offset, int blocks, byte[] newDataToWrite) {
        try {
            nfcV.connect();
            byte[] cmd = getCommandWriteSingleBlock(tagId);
            for (int i = 0; i < blocks; i++) {
                cmd[10] = (byte) ((offset + i) & 0x0ff);
                System.arraycopy(newDataToWrite, 4 * i, cmd, 11, 4);

                nfcV.transceive(cmd);
            }
            nfcV.close();
            Toast.makeText(activity, "Success to write to the tag. The new itemId is ", Toast.LENGTH_LONG).show();
        } catch (IOException ioException) {
            Toast.makeText(activity, "Failed to write to the tag", Toast.LENGTH_LONG).show();
        }
    }

    public static void check(Intent intent, Activity activity, boolean checkValue) {

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                byte[] cmd;
                if (!checkValue) {
                    cmd = getCommand(tagId, writeAFICommand, checkOutValue);
                } else {
                    cmd = getCommand(tagId, writeAFICommand, checkInValue);
                }
                nfcV.transceive(cmd);

                nfcV.close();
                if (!checkValue) {
                    Toast.makeText(activity, "Success to check out.", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(activity, "Success to check in.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException ioException) {
                if (!checkValue) {
                    Toast.makeText(activity, "Failed to check out.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "Failed to check in", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public static <T> void enableNFCInForeground(NfcAdapter nfcAdapter, Activity activity, Class<T> classType) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity, 0,
                new Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );
        IntentFilter nfcIntentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] filters = {nfcIntentFilter};
        String[][] techLists = {{NfcV.class.getName()}};

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists);
    }

    public static void disableNFCInForeground(NfcAdapter nfcAdapter, Activity activity) {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    private static byte[] getCommand(byte[] tagId, byte command, byte value) {

        byte[] cmd = new byte[]{
                flag,
                command,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                value,
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static byte[] getSystemInformation(byte[] tagId) {

        byte[] cmd = new byte[]{
                flag,
                getSystemInfoCommand,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static byte[] getCommandWriteSingleBlock(byte[] tagId) {

        byte[] cmd = new byte[]{
                flag,
                writeSingleBlockCommand,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static char[] setBarcode(String barcode, char[] currentData) {
        return Utilities.replaceStringAt(barcode, 2, 17, currentData);
    }

}
