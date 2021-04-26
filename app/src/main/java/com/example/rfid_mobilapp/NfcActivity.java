package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class NfcActivity extends AppCompatActivity {
    private static final String TAG = NfcActivity.class.getSimpleName();
    Button setting_button;
    NfcAdapter mNfcAdapter;
    static String newItemId;
    static String doCheckIn;
    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setting_button= findViewById(R.id.setting_button);
        setUpSettingButton();

        Intent startIntent= getIntent();
        Intent tagIntent= startIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        newItemId= startIntent.getStringExtra("newItemId");
        doCheckIn= startIntent.getStringExtra("doCheckIn");
        handleTag(tagIntent);
    }

    private void setUpSettingButton() {
        setting_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

        private void handleTag(Intent intent){
        if (doCheckIn != null) {
            Log.d(TAG, "will do check");
            if (doCheckIn.equals("false")) {
                NfcTagUtil.check(intent, this, checkOut);
                Log.d(TAG, "out");
            } else {
                NfcTagUtil.check(intent, this, checkIn);
                Log.d(TAG, "in");
            }
        } else if (newItemId != null && !newItemId.isEmpty()) {
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
        } else {
            NfcTagUtil.getItemId(intent, this);
        }
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcTagUtil.enableNFCInForeground(mNfcAdapter, this, getClass());
        Log.d(TAG, "on Resume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "on onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcTagUtil.disableNFCInForeground(mNfcAdapter, this);
        Log.d(TAG, "on pause");
    }

}