package com.example.rfid_mobilapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    String newItemId;
    String doCheckIn;
    Spinner spinner;
    Locale myLocale;
    String currentLanguage = "en", currentLang;
    ServerSocket server;
    Socket client;

    private static final boolean checkIn = true;
    private static final boolean checkOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //try {
        //ServerSocket server = new ServerSocket(8080);
        //Log.d(TAG, "Server has started on localhost.\r\nWaiting for a connection...");

            Thread thread = new Thread(() -> {
                String host = "localhost";
                int port = 8888;
                try  {
                    InetSocketAddress listenAddress = new InetSocketAddress(host, port);
                    SocketServer server = new SocketServer(listenAddress);
                    server.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();

          /* InputStream in = client.getInputStream();
            OutputStream out = client.getOutputStream();
            Scanner s = new Scanner(in, "UTF-8");
            String data = s.useDelimiter("\\r\\n\\r\\n").next();
            Matcher get = Pattern.compile("^GET").matcher(data);
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                match.find();
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                        + "\r\n\r\n").getBytes("UTF-8");
                out.write(response, 0, response.length);
        doCheckIn = uri.getQueryParameter("doCheckIn");
    }
    chooseLanguage();*/
        //} catch (IOException ioException) {
        //    ioException.printStackTrace();
        //}
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
    }*/

}