package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    String newItemId;
    String doCheckIn;
    tagInsertDialog dialog;
    Spinner spinner;

    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getIds();
        Resources res = getResources();

        LanguageUtil.getLanguageStruff(this, spinner, "en", res);
        Intent intent = getIntent();
        newItemId = "";
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String itemId = uri.getQueryParameter("itemid");
            tagContentTextView.setText("item id: " + itemId);
            newItemId = itemId;
            doCheckIn = uri.getQueryParameter("doCheckIn");
        }
        openHelpUserDialog();
    }


    @Override
    protected void onResume() {
        super.onResume();
        NfcTagUtil.enableNFCInForeground(mNfcAdapter, this, getClass());
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcTagUtil.disableNFCInForeground(mNfcAdapter, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (doCheckIn != null) {
            if (doCheckIn.equals("false")) {
                NfcTagUtil.check(intent, this, checkOut);
            } else {
                NfcTagUtil.check(intent, this, checkIn);
            }
            doCheckIn = null;
            newItemId = "";
        } else if (newItemId != "") {
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
            newItemId = "";
        } else {
            tagContentTextView.setText("");
            String payload = NfcTagUtil.getItemId(intent, this);
            tagContentTextView.setText(payload);

            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://aml-abbas.github.io/Quria/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, "Item Id: " + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }
        dialog.dismiss();
    }

    private void openHelpUserDialog() {
        dialog = new tagInsertDialog();
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    private void getIds() {
        tagContentTextView = findViewById(R.id.tagContentTextView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;
        spinner = (Spinner) findViewById(R.id.spinner);
    }
}