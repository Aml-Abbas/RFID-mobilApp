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

                byte[] oldData = readFirstBlock(tagId, nfcV, activity);

                byte[] primeItemId = new byte[16];
                boolean alternativItemId= false;

                byte[] primeItemId2 = new byte[16];
                Utilities.copyByteArray(oldData, 2, primeItemId, 0, 16);
                if (Utilities.isEmpty(primeItemId)){
                    return "No Id";
                }
                String stringOfPrimaryId = new String(primeItemId, StandardCharsets.UTF_8);

                if (stringOfPrimaryId.charAt(0) == '1') {
                    alternativItemId= true;
                  //  byte[] OptionalBlock = nfcV.transceive(getCommandReadSingleBlock(tagId));
                //    Utilities.copyByteArray(OptionalBlock, 4, primeItemId2, 0, 16);

                }
                if (!alternativItemId) {
                    payloadString = new String(primeItemId, StandardCharsets.UTF_8);
                } else {
                    payloadString = new String(primeItemId, StandardCharsets.UTF_8)+
                            new String(primeItemId2, StandardCharsets.UTF_8);
                }

        }
        return payloadString;
    }

    private static byte[] readFirstBlock(byte[] tagId, NfcV nfcV, Activity activity) {
        byte[] oldData = new byte[34];
        try {
           nfcV.connect();
           int offset = 0;
           byte[] cmdRead = getCommand(tagId, readSingleBlockCommand, (byte) 0x00);
           for (int i = 0; i < 8; i++) {
               cmdRead[10] = (byte) ((offset + i) & 0x0ff);
               byte[] response = nfcV.transceive(cmdRead);
               Utilities.copyByteArray(response, 1, oldData, i * 4, 4);
           }
           nfcV.close();
       }catch (IOException ioException){
           Toast.makeText(activity , "Failed to read the first block", Toast.LENGTH_LONG).show();
       }
        return oldData;
    }


    public static void writeNewItemId(String itemId, Intent intent, Activity activity) {
        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                int blockSize = 4;
                int amountOfBlocksToRead = 8;
                int offset = 0;
                byte[] oldData = new byte[34];

                byte[] cmdRead = getCommand(tagId, readSingleBlockCommand, (byte) 0x00);
                for (int i = 0; i < amountOfBlocksToRead; i++) {
                    cmdRead[10] = (byte) ((offset + i) & 0x0ff);
                    byte[] response = nfcV.transceive(cmdRead);
                    Utilities.copyByteArray(response, 0, oldData, i * 4, 4);
                }

                char[] newData = Utilities.initdata(oldData);
                char[] newDataWithBarcode = NfcTagUtil.setBarcode(itemId, newData);
                byte[] newDataToWrite = new String(newDataWithBarcode).getBytes(StandardCharsets.UTF_8);

                int blocks = newDataToWrite.length / blockSize;

                byte[] cmd = getCommandWriteSingleBlock(tagId);
                for (int i = 0; i < blocks; i++) {
                    cmd[10] = (byte) ((offset + i) & 0x0ff);
                    System.arraycopy(newDataToWrite, blockSize * i, cmd, 11, blockSize);

                    nfcV.transceive(cmd);
                }
                nfcV.close();
                Toast.makeText(activity, "Success to write to the tag. The new itemId is " + itemId, Toast.LENGTH_LONG).show();
            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to write to the tag", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void check(Intent intent, Activity activity, String checkValue) {

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                byte[] cmd;
                if (checkValue.equals("false")){
                    cmd = getCommand(tagId, writeAFICommand, checkOutValue);
                }else {
                   cmd = getCommand(tagId, writeAFICommand, checkInValue);
                }
                nfcV.transceive(cmd);

                nfcV.close();
                if (checkValue.equals("false")) {
                    Toast.makeText(activity, "Success to check out.", Toast.LENGTH_LONG).show();

                }else {
                    Toast.makeText(activity, "Success to check in.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException ioException) {
                if (checkValue.equals("false")) {
                    Toast.makeText(activity, "Failed to check out.", Toast.LENGTH_LONG).show();
                }else {
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
