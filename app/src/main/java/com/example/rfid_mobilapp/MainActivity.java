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
    static String newItemId;
    static String doCheckIn;
    static String doReadTagInfo;
    private boolean isActivityVisible;

    private SharedPreferences langPreferences;
    private final String LANG_PREF_KEY = "language";
    private final String LANGUAGE_SWEDISH = "sv";
    private final String LANGUAGE_ENGLISH = "en";

    private SharedPreferences switchPreferences;
    private final String SWITCH_STATE = "switchState";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " on create");
        onCreateHelper();
    }

    private void setUpSocketServiceSwitch() {
        socketServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               // switchPreferences.edit().putBoolean(SWITCH_STATE, isChecked).apply();
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
        isActivityVisible= false;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "on onDestroy");
        if (socketServiceSwitch.isChecked()){
            Log.d(TAG, "stop server on destroy");
            socketServiceSwitch.setChecked(false);
            stopService(serviceIntent);
            switchPreferences.edit().putBoolean(SWITCH_STATE, true).apply();
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            finish();
        }
        socketServiceSwitch.setChecked(false);
        stopService(serviceIntent);
        switchPreferences.edit().putBoolean(SWITCH_STATE, false).apply();
        Log.d(TAG, "pre false");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "new intent");
        if (socketServiceSwitch.isChecked() && intent.getAction() !=null &&
                (intent.getAction() == NfcAdapter.ACTION_TAG_DISCOVERED||
                intent.getAction() == NfcAdapter.ACTION_TECH_DISCOVERED) &&
                !isActivityVisible) {
                Log.d(TAG, "is checked");
                Intent startNfcActivityIntent = new Intent(this, NfcActivity.class);
                startNfcActivityIntent.putExtra(Intent.EXTRA_INTENT, intent);
                startNfcActivityIntent.putExtra("newItemId", newItemId);
                startNfcActivityIntent.putExtra("doCheckIn", doCheckIn);
                startNfcActivityIntent.putExtra("doReadTagInfo", doReadTagInfo);
                startActivity(startNfcActivityIntent);
            }
        doCheckIn = null;
        doReadTagInfo = null;
        newItemId = null;
    }

    public static void setItemId(String itemId) {
        if (itemId.equals("null")){
            newItemId= null;
        }else {
            newItemId = itemId;
        }
        doCheckIn = null;
        doReadTagInfo= null;
    }

    public static void setDoCheckIn(String value) {
        if (value.equals("null")){
            doCheckIn= null;
        }else {
            doCheckIn = value;
        }
        doReadTagInfo= null;
        newItemId = null;
    }

    public static void setDoReadTagInfo(String value) {
        if (value.equals("null")){
            doReadTagInfo= null;
        }else {
            doReadTagInfo = value;
        }
        newItemId = null;
        doCheckIn = null;
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "on onReStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "on Resume");
        isActivityVisible= true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "on onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        langPreferences = getSharedPreferences(LANG_PREF_KEY, MODE_PRIVATE);
        if (langPreferences != null) {
            setLocale(langPreferences.getString(LANG_PREF_KEY, LANGUAGE_ENGLISH));
        }
        setContentView(R.layout.activity_main);
        getIds();
        setUpSpinner();
        quriaText.setText(R.string.quria_off);
        switchPreferences = getSharedPreferences(SWITCH_STATE, MODE_PRIVATE);
        if (switchPreferences != null){
            boolean previousState= switchPreferences.getBoolean(SWITCH_STATE, false);
            if (previousState){
                quriaText.setText(R.string.quria_on);
                socketServiceSwitch.setChecked(true);
                serviceIntent = new Intent(this, SocketServerService.class);
                startService(serviceIntent);
            }
        }
        setUpSocketServiceSwitch();
    }

    private void setUpSpinner() {
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
                            langPreferences.edit().putString(LANG_PREF_KEY, LANGUAGE_ENGLISH).apply();
                            onCreateHelper();
                        }
                        break;
                    case 2:
                        if (getResources().getConfiguration().getLocales().get(0).getLanguage().equals(LANGUAGE_SWEDISH))
                            Toast.makeText(MainActivity.this, R.string.same_language, Toast.LENGTH_SHORT).show();
                        else {
                            langPreferences.edit().putString(LANG_PREF_KEY, LANGUAGE_SWEDISH).apply();
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