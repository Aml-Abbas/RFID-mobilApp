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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
                byte[] response = nfcV.transceive(getCommand(tagId, 0, 0));
                byte[] primeItemId;
                byte[] primeItemId2 = new byte[16];

                System.out.println("the respons size is: " + response.length);

                primeItemId = copyByteArray(response, 3);


                if (primeItemId[0] == 1) {

                    byte[] OptionalBlock = nfcV.transceive(getCommand(tagId, 32, 0));
                    primeItemId2 = copyByteArray(OptionalBlock, 5);
                    noId = false;
                } else {
                    noId = isEmtpy(primeItemId);
                }

                if (noId) {
                    payloadString = new String("NO ID");
                } else if (primeItemId2.length == 0) {

                    payloadString = String.valueOf(getResult(primeItemId));
                } else {
                    byte[] newPrimeItemId = new byte[16 * 2];

                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i] = primeItemId[i];
                    }
                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i + 16] = primeItemId[i];
                    }
                    payloadString = String.valueOf(getResult(newPrimeItemId));
                }
                nfcV.close();

            } catch (IOException ioException) {
                Toast.makeText(activity, "Failed to read tag", Toast.LENGTH_LONG).show();
            }
        }
        return payloadString;
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

    private static byte[] getCommand(byte[] tagId, int offset, int blocks) {

        /* the code is taken from
        https://stackoverflow.com/questions/55856674/writing-single-block-command-fails-over-nfcv*/

        byte[] cmd = new byte[]{
                (byte) 0x60,  // flags: addressed (= UID field present)
                (byte) 0x23, // command: READ MULTIPLE BLOCKS
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // placeholder for tag UID
                (byte) (offset & 0x0ff),  // first block number
                (byte) ((blocks - 1) & 0x0ff)  // number of blocks (-1 as 0x00 means one block)
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static int getResult(byte[] primeItemId) {
        ByteBuffer buffer = ByteBuffer.wrap(primeItemId);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int result = buffer.getShort();
        return result;
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
        for (int i = 0; i < 6; i++) {
            toArray[i] = fromArray[i + fromIndex];
        }
        return toArray;
    }
}
