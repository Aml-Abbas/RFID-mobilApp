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
    String doCheckIn;
    Spinner spinner;
    Locale myLocale;
    String currentLanguage ="en", currentLang;
    TagProgressDialog dialog;

    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openDialog();
        setContentView(R.layout.activity_main);
        getIds();
        tagContentTextView.setText(R.string.place_tag);
        Intent intent = getIntent();
        newItemId = "";
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String itemId = uri.getQueryParameter("itemid");
            newItemId = itemId;
            doCheckIn = uri.getQueryParameter("doCheckIn");
        }
        chooseLanguage();
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
        }/* else {
            tagContentTextView.setText("");
            String payload = NfcTagUtil.getItemId(intent, this);
            tagContentTextView.setText(payload);

            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://aml-abbas.github.io/RFID-mobilApp/Quria/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, "Item Id: " + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }*/
    }

    private void getIds() {
        tagContentTextView = findViewById(R.id.tagContentTextView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;
        spinner = (Spinner) findViewById(R.id.spinner);
    }
    private void chooseLanguage() {
        currentLanguage = getIntent().getStringExtra(currentLang);
        List<String> list = new ArrayList<String>();

        list.add("Select language");
        list.add("English");
        list.add("Svenska");


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("en");
                        break;
                    case 2:
                        setLocale("sv");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(MainActivity.this, R.string.same_language, Toast.LENGTH_SHORT).show();
        }
    }
    private void openDialog(){
        dialog = new TagProgressDialog();
        dialog.show(getSupportFragmentManager(), "example dialog");
    }
}