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
    static Intent serviceIntent;

    private static final byte flagAddressedCommand = (byte) 0x20;

    private static final byte writeAFICommand = (byte) 0x27;
    private static final byte writeSingleBlockCommand = (byte) 0x21;
    private static final byte readSingleBlockCommand = (byte) 0x20;
    private static final byte getSystemInfoCommand = (byte) 0x2B;

    private static final byte checkInValue = (byte) 0x07;
    private static final byte checkOutValue = (byte) 0xC2;

    public static void getItemId(Intent intent, Activity activity) {
        String payloadString = "";
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (intent != null && tag != null) {
            NfcV nfcV = NfcV.get(tag);
            byte[] tagId = tag.getId();

            byte[] dataRead = readBlocks(tagId, nfcV, activity, 0, 8);

            byte[] primeItemId = new byte[16];

            Utilities.copyByteArray(dataRead, 3, primeItemId, 0, 16);
            if (Utilities.isEmpty(primeItemId)) {
                payloadString = "no id";
            } else {
                String stringOfPrimaryId = new String(primeItemId, StandardCharsets.UTF_8);

                if (stringOfPrimaryId.charAt(0) == '1') {
                    byte[] primeItemIdExtended = new byte[16];
                    byte[] OptionalBlock = readBlocks(tagId, nfcV, activity, 8, 6);
                    Utilities.copyByteArray(OptionalBlock, 4, primeItemIdExtended, 0, 16);
                    payloadString = new String(primeItemId, StandardCharsets.UTF_8) +
                            new String(primeItemIdExtended, StandardCharsets.UTF_8);
                } else {
                    payloadString = new String(primeItemId, StandardCharsets.UTF_8);
                }
            }
        }
        serviceIntent = new Intent(activity, SocketServerService.class);
        serviceIntent.setAction("READ_ITEM_ID");
        serviceIntent.putExtra("itemId", payloadString.trim());
        activity.startService(serviceIntent);
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
            Toast.makeText(activity, R.string.failed_read, Toast.LENGTH_LONG).show();
        }
        return resultData;
    }

    public static void writeNewItemId(String itemId, Intent intent, Activity activity) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String status;

        if (intent != null && tag != null) {
            NfcV nfcV = NfcV.get(tag);
            byte[] tagId = tag.getId();
            byte[] oldData = readBlocks(tagId, nfcV, activity, 0, 8);
            byte[] newDataWithBarcode = NfcTagUtil.setBarcode(itemId, oldData);
            int CRCValue = Utilities.calculateCRC16(Utilities.getDataWithoutCRC(newDataWithBarcode));
            byte[] newDataToWrite = setCRC(CRCValue, newDataWithBarcode);
            status = writeBlocks(tagId, nfcV, activity, 0, 8, newDataToWrite) + " trying to write item Id.";
        } else {
            status = activity.getResources().getString(R.string.failed_write) + " item Id was not written.";
        }
        serviceIntent = new Intent(activity, SocketServerService.class);
        serviceIntent.setAction("WRITE_ITEM_ID");
        serviceIntent.putExtra("itemId", status);
        activity.startService(serviceIntent);
    }

    private static String writeBlocks(byte[] tagId, NfcV nfcV, Activity activity, int offset, int blocks, byte[] newDataToWrite) {
        try {
            nfcV.connect();
            byte[] cmd = getCommandWriteSingleBlock(tagId);
            for (int i = 0; i < blocks; i++) {
                cmd[10] = (byte) ((offset + i) & 0x0ff);
                System.arraycopy(newDataToWrite, 4 * i, cmd, 11, 4);
                nfcV.transceive(cmd);
            }
            nfcV.close();
            return activity.getResources().getString(R.string.success_write);
        } catch (IOException ioException) {
        }
        return activity.getResources().getString(R.string.failed_write);
    }

    public static void check(Intent intent, Activity activity, boolean doCheckIn) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String status;

        if (intent != null && tag != null) {
            NfcV nfcV = NfcV.get(tag);

            try {
                nfcV.connect();
                byte[] tagId = tag.getId();
                byte checkInOrOutValue = doCheckIn ? checkInValue : checkOutValue;
                byte[] cmd = getCommand(tagId, writeAFICommand, checkInOrOutValue);
                nfcV.transceive(cmd);

                nfcV.close();
                status = doCheckIn ? activity.getResources().getString(R.string.success_checkin)
                        : activity.getResources().getString(R.string.success_checkout);
            } catch (IOException ioException) {
                status = doCheckIn ? activity.getResources().getString(R.string.failed_checkin)
                        : activity.getResources().getString(R.string.failed_checkout);

            }
            serviceIntent = new Intent(activity, SocketServerService.class);
            serviceIntent.setAction("CHECK");
            serviceIntent.putExtra("doCheckIn", status);
            activity.startService(serviceIntent);
        }
    }


    public static <T> void enableNFCInForeground(NfcAdapter nfcAdapter, Activity activity, Class<T> classType) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                activity, 0,
                new Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );
        IntentFilter nfcIntentFilter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter nfcIntentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] filters = {nfcIntentFilter, nfcIntentFilter2};
        String[][] techLists = {{NfcV.class.getName()}};

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists);
    }

    public static void disableNFCInForeground(NfcAdapter nfcAdapter, Activity activity) {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    private static byte[] getCommand(byte[] tagId, byte command, byte value) {

        byte[] cmd = new byte[]{
                flagAddressedCommand,
                command,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                value,
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static byte[] getSystemInformation(byte[] tagId) {

        byte[] cmd = new byte[]{
                flagAddressedCommand,
                getSystemInfoCommand,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    private static byte[] getCommandWriteSingleBlock(byte[] tagId) {

        byte[] cmd = new byte[]{
                flagAddressedCommand,
                writeSingleBlockCommand,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

    public static byte[] setBarcode(String barcode, byte[] currentData) {
        return Utilities.replaceBarcode(barcode, 3, 16, currentData);
    }

    public static byte[] setCRC(int CRC, byte[] currentData) {
        return Utilities.replaceCRC(CRC, currentData);
    }
}
