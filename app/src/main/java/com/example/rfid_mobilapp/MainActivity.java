package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Spinner spinner;
    static Switch socketServiceSwitch;
    TextView quriaText;
    Intent serviceIntent;
    Locale myLocale;
    NfcAdapter mNfcAdapter;
    static String newItemId;
    static String doCheckIn;
    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    private SharedPreferences preferences;
    private final String LANG_PREF_KEY = "language";
    private final String LANGUAGE_SWEDISH = "sv";
    private final String LANGUAGE_ENGLISH = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " on create");
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        onCreateHelper();
    }

    private void setUpSocketServiceSwitch() {
        socketServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    quriaText.setText(R.string.quria_on);
                    serviceIntent = new Intent(MainActivity.this, SocketServerService.class);
                    startService(serviceIntent);
                } else {
                    quriaText.setText(R.string.quria_off);
                    stopService(serviceIntent);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
        startActivity(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "on onDestroy");
        stopService(serviceIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (socketServiceSwitch.isChecked()){
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
        }
        moveTaskToBack(true);
    }

    public static void setItemId(String itemId) {
        Log.d(TAG, "1. item id is now " + itemId);
        newItemId = itemId;
    }

    public static void setDoCheckIn(String value) {
        Log.d(TAG, "value now is " + value);
        doCheckIn = value;
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

    private void getIds() {
        spinner = findViewById(R.id.spinner);
        socketServiceSwitch = findViewById(R.id.stopSocketServiceButton);
        quriaText= findViewById(R.id.Quria_text);
    }

    public void setLocale(String localeName) {
        myLocale = new Locale(localeName);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.setLocale(myLocale);
        resources.updateConfiguration(config, displayMetrics);
    }

    private void onCreateHelper() {
        preferences = getSharedPreferences(LANG_PREF_KEY, MODE_PRIVATE);
        if (preferences != null) {
            setLocale(preferences.getString(LANG_PREF_KEY, LANGUAGE_ENGLISH));
        }
        setContentView(R.layout.activity_main);
        getIds();
        quriaText.setText(R.string.quria_on);
        setUpSpinner(spinner);
        serviceIntent = new Intent(this, SocketServerService.class);
        startService(serviceIntent);
        setUpSocketServiceSwitch();
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
                            onCreateHelper();
                        }
                        break;
                    case 2:
                        if (getResources().getConfiguration().getLocales().get(0).getLanguage().equals(LANGUAGE_SWEDISH))
                            Toast.makeText(MainActivity.this, R.string.same_language, Toast.LENGTH_SHORT).show();
                        else {
                            preferences.edit().putString(LANG_PREF_KEY, LANGUAGE_SWEDISH).apply();
                            onCreateHelper();
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