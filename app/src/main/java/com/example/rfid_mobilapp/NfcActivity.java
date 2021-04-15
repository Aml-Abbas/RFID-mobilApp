package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;

public class NfcActivity extends AppCompatActivity {
    private static final String TAG = NfcTagUtil.class.getSimpleName();
    NfcAdapter mNfcAdapter;
    static String newItemId;
    static String doCheckIn;
    TagProgressDialog dialog;
    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        openDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "new intent");
        if (doCheckIn != null) {
            Log.d(TAG, "will do check");
            if (doCheckIn.equals("false")) {
                NfcTagUtil.check(intent, this, checkOut);
                Log.d(TAG, "out");
            } else {
                NfcTagUtil.check(intent, this, checkIn);
                Log.d(TAG, "in");
            }
            doCheckIn = null;
            newItemId = "";
        } else if (newItemId != null && !newItemId.isEmpty()) {
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
            newItemId = "";
        } else {
            NfcTagUtil.getItemId(intent, this);
        }
        moveTaskToBack(true);
    }

    public static void setItemId(String itemId) {
        Log.d(TAG, "1. item id is now" + itemId);
        newItemId = itemId;
    }

    public static void setDoCheckIn(String value) {
        Log.d(TAG, "value now is " + value);
        doCheckIn = value;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "on onReStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcTagUtil.enableNFCInForeground(mNfcAdapter, this, getClass());
        Log.d(TAG, "on Resume");
        moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "on onStart");
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcTagUtil.disableNFCInForeground(mNfcAdapter, this);
        Log.d(TAG, "on pause");
    }

    private void openDialog() {
        dialog = new TagProgressDialog();
        dialog.show(getSupportFragmentManager(), "example dialog");
    }
}