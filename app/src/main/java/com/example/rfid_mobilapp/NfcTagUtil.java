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
                for (int i=0; i<response.length;i++){
                    Log.d("respons is:" , String.valueOf(response[i]));
                }
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
                byte[] data= itemId.getBytes();
                byte[] newData= new byte[18];
                for (int i=0; i<data.length;i++){
                    Log.d("data is:" , String.valueOf(data[i]));
                }
                for ( int i=0; i<data.length;i++){
                    newData[i+2]= data[i];
                }
                for (int i=0; i<newData.length;i++){
                    Log.d("newData is:" , String.valueOf(newData[i]));
                }
                newData = Arrays.copyOfRange(newData, 0, 4 * 8 );
                for (int i=0; i<newData.length;i++){
                    Log.d("new newData is:" , String.valueOf(newData[i]));
                }
                int offset =0;
                byte[] cmd = new byte[] {
                        (byte)0x20,
                        (byte)0x21,
                        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                        (byte)0x00,
                        /* DATA    */ (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
                };
                System.arraycopy(tagId, 0, cmd, 2, 8);

                for (int i = 0; i < 8; ++i) {
                    cmd[10] = (byte) i;
                    System.arraycopy(newData, 4 * i, cmd, 11, 4);
                    for (int k=0; k<cmd.length;k++){
                        Log.d("cmd is: i: "+i+" k: "+k , String.valueOf(cmd[k]));

                    }
                    nfcV.transceive(cmd);
                }
             //   byte[] response = nfcV.transceive(getCommandWriteSingleBlock(tagId, itemId, 0));
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

}
