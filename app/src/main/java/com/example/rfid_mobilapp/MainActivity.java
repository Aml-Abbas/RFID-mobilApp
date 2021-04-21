package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    TextView timer;
    TextView openConnection;
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
                    openConnection.setText(R.string.reopen_connection);
                    showTimer();
                    socketServiceSwitch.setClickable(false);
                    stopService(serviceIntent);
                }
            }
        });
    }

    private void showTimer() {
        long maxTimeInMilliseconds = 60000;
        CountDownTimer t;
        t = new CountDownTimer(maxTimeInMilliseconds, 1000) {

            public void onTick(long millisUntilFinished) {
                long remainedSecs = millisUntilFinished / 1000;
                timer.setText("" + (remainedSecs / 60) + ":" + (remainedSecs % 60));// manage it accordign to you
            }

            public void onFinish() {
                openConnection.setText("");
                timer.setText("");
                socketServiceSwitch.setClickable(true);
                cancel();
            }
        }.start();
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

    private void getIds() {
        spinner = findViewById(R.id.spinner);
        socketServiceSwitch = findViewById(R.id.stopSocketServiceButton);
        quriaText= findViewById(R.id.Quria_text);
        openConnection= findViewById(R.id.open_connectin);
        timer= findViewById(R.id.timer);

    }

    public static boolean isServerOn() {
        if (socketServiceSwitch != null) {
            return socketServiceSwitch.isChecked();
        }
        return false;
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
        openConnection.setText("");
        timer.setText("");
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