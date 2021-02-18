package com.example.rfid_mobilapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NfcTagUtil {


    public static String getPayload(Intent intent, Activity activity) {
        String payloadString = "test";

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            boolean noId = true;

            try {
                nfcV.connect();

                byte[] tagId = tag.getId();
                byte[] response = nfcV.transceive(getCommandReadMultipleBlock(tagId, 0, 0));

                byte[] primeItemId;
                byte[] primeItemId2 = new byte[16];
                primeItemId = copyByteArray(response, 2);
                for (int i=0; i<primeItemId.length;i++){
                    Log.d("prime is:" , String.valueOf(primeItemId[i]));
                }
                if (primeItemId[0] == 1) {

                    byte[] OptionalBlock = nfcV.transceive(getCommandReadMultipleBlock(tagId, 32, 0));
                    primeItemId2 = copyByteArray(OptionalBlock, 4);

                    noId = false;
                } else {
                    noId = isEmtpy(primeItemId);
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
                int maxDataAmount = nfcV.getMaxTransceiveLength(); // Using one of my tags this results in 253
                int blockSize = 4; // This can be fetched/identified for some ISO15693 tags using system info.
                int amountOfBlocksToRead = maxDataAmount / blockSize;
                int offset = 0;
                amountOfBlocksToRead = 8; // original amountOfBlocksToRead might be 63 (253/4), but lets settle with 8 blocks since we know the barcode fits well within.
                byte[] oldData = nfcV.transceive(getCommandReadMultipleBlock(tagId, 0, amountOfBlocksToRead));
                char[] newData = NfcTagUtil.initdata(oldData);
                char[] newDataWithBarcode = NfcTagUtil.setBarcode(itemId, newData);
                byte[] newDataToWrite = new String(newDataWithBarcode).getBytes(StandardCharsets.UTF_8);
                int blocks = newDataToWrite.length / blockSize;

                byte[] cmd = getCommandWriteSingleBlock(tagId);
                for (int i = 0; i < blocks; ++i) {
                    cmd[10] = (byte)((offset + i) & 0x0ff);
                    System.arraycopy(newDataToWrite, blockSize * i, cmd, 11, blockSize);

                    byte[] response = nfcV.transceive(cmd);
                    Log.d("NfcTagUtil", "Write response: " + response);
                }
                nfcV.close();
                Toast.makeText(activity, "Success to write to the tag. The new itemId is "+itemId , Toast.LENGTH_LONG).show();
            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to write to the tag", Toast.LENGTH_LONG).show();
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

    private static byte[] getCommandReadMultipleBlock(byte[] tagId, int offset, int blocks) {

        /* the code is taken from
        https://stackoverflow.com/questions/55856674/writing-single-block-command-fails-over-nfcv
        */
        byte[] cmd = new byte[]{
                (byte) 0x60,
                (byte) 0x23,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) (offset & 0x0ff),
                (byte) ((blocks - 1) & 0x0ff)
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }


    private static boolean isEmtpy(byte[] primeItemId) {
        for (int i = 0; i < primeItemId.length; i++) {
            if (primeItemId[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static byte[] copyByteArray(byte[] fromArray, int fromIndex) {
        byte[] toArray = new byte[16];
        for (int i = 0; i < 16; i++) {
            toArray[i] = fromArray[i + fromIndex];
        }
        return toArray;
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
        return replaceStringAt(barcode, 3, 16, currentData);
    }

    private static char[] replaceStringAt(String stringValue, int start, int len, char[] currentData) {
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
