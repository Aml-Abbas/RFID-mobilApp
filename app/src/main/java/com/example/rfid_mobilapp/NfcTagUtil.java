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


    public static String getItemId(Intent intent, Activity activity) {
        String payloadString = "test";

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            boolean noId = true;

            try {
                nfcV.connect();

                byte[] tagId = tag.getId();

                int offset = 0;
                byte[] oldData = new byte[34];
                byte[] cmdRead = getCommandReadSingleBlock(tagId);
                for (int i = 0; i < 8; i++) {
                    cmdRead[10] = (byte)((offset + i) & 0x0ff);
                    byte[] response = nfcV.transceive(cmdRead);
                    copyByteArray(response, 1, oldData, i* 4, 4);
                }

                byte[] primeItemId= new byte[16];
                byte[] primeItemId2 = new byte[16];
                copyByteArray(oldData, 2, primeItemId, 0, 16);
                if (primeItemId[0] == 1) {

                    byte[] OptionalBlock = nfcV.transceive(getCommandReadSingleBlock(tagId));
                   copyByteArray(OptionalBlock, 4, primeItemId2, 0, 16);

                    noId = false;
                } else {
                    noId = isEmpty(primeItemId);
                }

                if (noId) {
                    payloadString ="NO ID";
                } else if (primeItemId[0] != 1) {
                    payloadString = new String(primeItemId, StandardCharsets.UTF_8);
                } else {
                    byte[] newPrimeItemId = new byte[16 * 2];

                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i] = primeItemId[i];
                    }
                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i + 16] = primeItemId2[i];
                    }
                    payloadString = new String(newPrimeItemId, StandardCharsets.UTF_8);
                }
                nfcV.close();

            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to read tag", Toast.LENGTH_LONG).show();
            }
        }
        return payloadString;
    }

    public static void writeNewItemId(String itemId, Intent intent, Activity activity){
        if (intent != null) {
           Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                int maxDataAmount = nfcV.getMaxTransceiveLength();
                int blockSize = 4;
                int amountOfBlocksToRead =8;
                int offset = 0;
                byte[] oldData = new byte[34];

                byte[] cmdRead = getCommandReadSingleBlock(tagId);
                for (int i = 0; i < amountOfBlocksToRead; i++) {
                    cmdRead[10] = (byte)((offset + i) & 0x0ff);
                    byte[] response = nfcV.transceive(cmdRead);
                    copyByteArray(response, 0, oldData, i* 4, 4);
                }

                char[] newData = NfcTagUtil.initdata(oldData);
                char[] newDataWithBarcode = NfcTagUtil.setBarcode(itemId, newData);
                byte[] newDataToWrite = new String(newDataWithBarcode).getBytes(StandardCharsets.UTF_8);

                int blocks = newDataToWrite.length / blockSize;

                byte[] cmd = getCommandWriteSingleBlock(tagId);
                for (int i = 0; i < blocks; i++) {
                    cmd[10] = (byte)((offset + i) & 0x0ff);
                    System.arraycopy(newDataToWrite, blockSize * i, cmd, 11, blockSize);

                    nfcV.transceive(cmd);
                }
                nfcV.close();
                Toast.makeText(activity, "Success to write to the tag. The new itemId is "+itemId , Toast.LENGTH_LONG).show();
            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to write to the tag", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static void checkIn(Intent intent, Activity activity){
        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                byte[] cmd= getCommandCheckIn(tagId);
                nfcV.transceive(cmd);

                nfcV.close();
                Toast.makeText(activity, "Success to check in." , Toast.LENGTH_LONG).show();
            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to check in", Toast.LENGTH_LONG).show();
            }
        }
    }


    public static void checkOut(Intent intent, Activity activity){
        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                byte[] cmd= getCommandCheckOut(tagId);

                nfcV.transceive(cmd);

                nfcV.close();
                Toast.makeText(activity, "Success to check out." , Toast.LENGTH_LONG).show();
            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to check out", Toast.LENGTH_LONG).show();
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

    private static byte[] getCommandReadSingleBlock(byte[] tagId) {

        byte[] cmd = new byte[]{
                (byte) 0x22,
                (byte) 0x20,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte)0x00, /* OFFSET  */
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }


    private static byte[] getSystemInformation(byte[] tagId) {

        byte[] cmd = new byte[]{
                (byte) 0x20,
                (byte) 0x2B,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }
    private static byte[] getCommandCheckIn(byte[] tagId) {

        byte[] cmd = new byte[]{
                (byte) 0x20,
                (byte) 0x27,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x07,

        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }
    private static byte[] getCommandCheckOut(byte[] tagId) {

        byte[] cmd = new byte[]{
                (byte) 0x20,
                (byte) 0x27,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0xC2,

        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static boolean isEmpty(byte[] primeItemId) {
        for (int i = 0; i < primeItemId.length; i++) {
            if (primeItemId[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static void copyByteArray(byte[] fromArray, int fromIndex,
                                      byte[] toArray, int fromIndexTo, int length) {

        for (int i = 0 ; i < length; i++) {
            toArray[fromIndexTo+i] = fromArray[i + fromIndex];
        }
    }

    private static byte[] getCommandWriteSingleBlock(byte[] tagId) {

        byte[] cmd = new byte[] {
                (byte)0x20, /* FLAGS   */
                (byte)0x21, /* COMMAND */
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, /* UID     */
                (byte)0x00, /* OFFSET  */
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 /* DATA placeholder */
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static char[] initdata(byte[] in) {

        char[] userdata = new char[in.length];

        for (int i = 0; i < in.length; i++) {
            userdata[i]=(char)in[i];
        }
        return userdata;
    }

    private static char[] setBarcode(String barcode, char[] currentData) {
        return Utilities.replaceStringAt(barcode, 2, 17, currentData);
    }




}
