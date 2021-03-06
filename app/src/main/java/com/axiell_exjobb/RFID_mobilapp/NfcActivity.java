package com.axiell_exjobb.RFID_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NfcActivity extends AppCompatActivity {
    Button setting_button;
    NfcAdapter mNfcAdapter;
    String newItemId;
    String doCheckIn;
    String doReadTagInfo;
    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        setting_button = findViewById(R.id.setting_button);
        setUpSettingButton();

        Intent startIntent = getIntent();
        Intent tagIntent = startIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        newItemId = startIntent.getStringExtra("newItemId");
        doCheckIn = startIntent.getStringExtra("doCheckIn");
        doReadTagInfo = startIntent.getStringExtra("doReadTagInfo");
        handleTag(tagIntent);
    }

    private void setUpSettingButton() {
        setting_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void handleTag(Intent intent) {
        if (doCheckIn != null) {
            if (doCheckIn.equals("false")) {
                NfcTagUtil.check(intent, this, checkOut);
            } else {
                NfcTagUtil.check(intent, this, checkIn);
            }
        } else if (newItemId != null) {
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
        } else if (doReadTagInfo != null && doReadTagInfo.equals("true")) {
            NfcTagUtil.getItemId(intent, this);
        }
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcTagUtil.enableNFCInForeground(mNfcAdapter, this, getClass());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcTagUtil.disableNFCInForeground(mNfcAdapter, this);
    }

}