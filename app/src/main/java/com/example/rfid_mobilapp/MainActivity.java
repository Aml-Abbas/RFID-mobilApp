package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    Spinner spinner;
    static Switch socketServiceSwitch;
    Intent serviceIntent;
    Locale myLocale;
    private SharedPreferences preferences;
    private final String LANG_PREF_KEY = "language";
    private final String LANGUAGE_SWEDISH = "sv";
    private final String LANGUAGE_ENGLISH = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, " on create");
        preferences = getSharedPreferences("langpref", MODE_PRIVATE);
        if (preferences != null) {
            setLocale(preferences.getString(LANG_PREF_KEY, LANGUAGE_ENGLISH));
        }
        setContentView(R.layout.activity_main);
        getIds();
        setUpSpinner(spinner);
        setUSocketServiceButton();
        serviceIntent = new Intent(this, SocketServerService.class);
        startService(serviceIntent);
    }

    private void setUSocketServiceButton() {
        socketServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    serviceIntent = new Intent(MainActivity.this, SocketServerService.class);
                    startService(serviceIntent);
                } else {
                    stopService(serviceIntent);
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
        Intent intent = new Intent(this, NfcActivity.class);
        startActivity(intent);
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
        Log.d(TAG, "on pause");
    }

    public static boolean isServerOn() {
        if (socketServiceSwitch!=null){
            return socketServiceSwitch.isChecked();
        }
        return false;
    }

    private void getIds() {
        spinner = findViewById(R.id.spinner);
        socketServiceSwitch = findViewById(R.id.stopSocketServiceButton);
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