package com.example.rfid_mobilapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LanguageUtil {

    public static void getLanguageStruff(Activity activity, Spinner spinner, String currentLanguage, Resources resources) {

        List<String> list = new ArrayList<>();

        list.add("Select language");
        list.add("English");
        list.add("Svenska");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        LanguageUtil.setLocale(activity, currentLanguage, "en", resources);
                        break;
                    case 2:
                        LanguageUtil.setLocale(activity, currentLanguage, "sv", resources);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private static void setLocale(Activity activity, String currentLanguage, String localeName, Resources resources) {
        if (!localeName.equals(currentLanguage)) {
            Locale myLocale = new Locale(localeName);
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration conf = resources.getConfiguration();
            conf.locale = myLocale;
            resources.updateConfiguration(conf, dm);
            Intent refresh = new Intent(activity, MainActivity.class);
            refresh.putExtra(currentLanguage, localeName);
            activity.startActivity(refresh);
        } else {
            Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
        }
    }

}
