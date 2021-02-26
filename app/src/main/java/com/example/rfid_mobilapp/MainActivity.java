package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    String newItemId;
    String checkValue;
    tagInsertDialog dialog;

    Spinner spinner;
    String currentLanguage = "en", currentLang;

    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getIds();
        Resources res = getResources();

        LanguageUtil.getLanguageStruff(this, spinner, currentLanguage, res);
        Intent intent = getIntent();
        newItemId="";
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String itemId = uri.getQueryParameter("itemid");
            tagContentTextView.setText(R.string.show_id + itemId);
            newItemId= itemId;
            checkValue = uri.getQueryParameter("checkValue");
        }
        openDialog();
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
        if (checkValue!= null){
            if (checkValue.equals("false")){
                NfcTagUtil.check(intent, this, checkOut);
            }else {
                NfcTagUtil.check(intent, this, checkIn);
            }
            checkValue= null;
            newItemId= "";
        }else if(newItemId!= ""){
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
            newItemId="";
        }else {
            tagContentTextView.setText("");
            String payload = NfcTagUtil.getItemId(intent, this);
            tagContentTextView.setText(payload);

            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://aml-abbas.github.io/Quria/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, R.string.open_site + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }
        dialog.dismiss();
    }

    private void openDialog(){
        dialog = new tagInsertDialog();
        dialog.show(getSupportFragmentManager(), "dialog");
    }
private void getIds(){
    tagContentTextView = findViewById(R.id.tagContentTextView);
    mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    mainActivityContext = this;
    currentLanguage = getIntent().getStringExtra(currentLang);
    spinner = (Spinner) findViewById(R.id.spinner);
}
}