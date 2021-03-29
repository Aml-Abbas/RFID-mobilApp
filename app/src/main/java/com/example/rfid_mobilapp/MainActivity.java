package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    String newItemId;
    String doCheckIn;
    Spinner spinner;
    Switch stopSocketServiceButton;
    Intent serviceIntent;
    Locale myLocale;
    String currentLanguage = "en", currentLang;
    ServerSocket server;
    Socket client;

    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale("sv");
        setContentView(R.layout.activity_main);

        Log.d(TAG, "language: " + getResources().getConfiguration().getLocales().get(0).getLanguage());

        getIds();
        setUpSpinner(spinner);
        setUpStopSocketServiceButton();
        serviceIntent = new Intent(this, SocketServerService.class);
        startService(serviceIntent);
    }

    private void setUpStopSocketServiceButton() {
        stopSocketServiceButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(serviceIntent);
                } else {
                    stopService(serviceIntent);
                }
            }
        });
    }


     /*   @Override
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
            intent.setData(Uri.parse("https://aml-abbas.github.io/RFID-mobilApp/Quria/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, "Item Id: " + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }
    } */

    private void getIds() {
        //   mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;
        spinner = findViewById(R.id.spinner);
        stopSocketServiceButton = findViewById(R.id.stopSocketServiceButton);
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLang)) {
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(myLocale);
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(this, MainActivity.class);
            refresh.putExtra(currentLang, localeName);
            startActivity(refresh);
        } else {
            Toast.makeText(MainActivity.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpSpinner(Spinner spinner) {

        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("en");
                        Log.d(TAG, "en Selected");
                        Log.d(TAG, "language: " + getResources().getConfiguration().getLocales().get(0).getLanguage());

                        break;
                    case 2:
                        setLocale("sv");
                        Log.d(TAG, "SE Selected");
                        Log.d(TAG, "language: " + getResources().getConfiguration().getLocales().get(0).getLanguage());

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}