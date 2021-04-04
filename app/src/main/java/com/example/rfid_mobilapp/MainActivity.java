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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
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
    private static final String TAG = MainActivity.class.getSimpleName();
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    static String newItemId;
    static String doCheckIn;
    Spinner spinner;
    Switch stopSocketServiceButton;
    Intent serviceIntent;
    Locale myLocale;
    private SharedPreferences preferences;
    private final String LANG_PREF_KEY = "language";
    private final String LANGUAGE_SWEDISH = "sv";
    private final String LANGUAGE_ENGLISH = "en";


    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    public static void setItemId(String itemId) {
        Log.d(TAG, "item id is now"+ itemId);

        newItemId= itemId;
    }

    public static void setDoCheckIn(String value) {
        Log.d(TAG, "value now is "+ value);

        doCheckIn= value;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("langpref", MODE_PRIVATE);
        if (preferences != null) {
            setLocale(preferences.getString(LANG_PREF_KEY, LANGUAGE_ENGLISH));
        }

        setContentView(R.layout.activity_main);
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
        } /*else {
            String payload = NfcTagUtil.getItemId(intent, this);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://aml-abbas.github.io/RFID-mobilApp/Quria/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, "Item Id: " + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }*/
    }

    private void getIds() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;
        spinner = findViewById(R.id.spinner);
        stopSocketServiceButton = findViewById(R.id.stopSocketServiceButton);
    }

    public void setLocale(String localeName) {
        myLocale = new Locale(localeName);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(myLocale);
        resources.updateConfiguration(config, displayMetrics);
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
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
                        if (getResources().getConfiguration().getLocales().get(0).getLanguage().equals(LANGUAGE_ENGLISH))
                            Toast.makeText(MainActivity.this, R.string.same_language, Toast.LENGTH_SHORT).show();
                        else {
                            preferences.edit().putString(LANG_PREF_KEY, LANGUAGE_ENGLISH).apply();
                            restartActivity();
                        }
                        break;
                    case 2:
                        if (getResources().getConfiguration().getLocales().get(0).getLanguage().equals(LANGUAGE_SWEDISH))
                            Toast.makeText(MainActivity.this, R.string.same_language, Toast.LENGTH_SHORT).show();
                        else {
                            preferences.edit().putString(LANG_PREF_KEY, LANGUAGE_SWEDISH).apply();
                            restartActivity();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}