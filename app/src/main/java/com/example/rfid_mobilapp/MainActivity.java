package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import static java.nio.charset.StandardCharsets.UTF_8;


public class MainActivity extends AppCompatActivity {
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    PendingIntent mPendingIntent;
    public static byte[] GET_RANDOM = {
            (byte) 0x00, // CLA Class
            (byte) 0x84, // INS Instruction
            (byte) 0x00, // P1  Parameter 1
            (byte) 0x00, // P2  Parameter 2
            (byte) 0x08  // LE  maximal number of bytes expected in result
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagContentTextView = findViewById(R.id.tagContentTextView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }

    @Override
    protected void onResume() {
        super.onResume();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        );
        IntentFilter nfcIntentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] filters = {nfcIntentFilter};
        String[][] techLists = {{NfcV.class.getName()}};

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techLists);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tagContentTextView.setText("");

        tagContentTextView.setText(getPayload(intent));
    }

    private String getPayload(Intent intent) {
        String payloadString = "test";

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            boolean inAlternativeItemId = false;
            boolean noId = false;

            try {
                nfcV.connect();

                byte[] tagId = tag.getId();
                int blockAddress = 0;
                byte[] cmd = new byte[] {
                        (byte)0x20,  // FLAGS
                        (byte)0x20,  // READ_SINGLE_BLOCK
                        0, 0, 0, 0, 0, 0, 0, 0,
                        (byte)(blockAddress & 0x0ff)
                };
                System.arraycopy(tagId, 0, cmd, 2, 8);

                byte[] response = nfcV.transceive(cmd);

/*                byte[] primeItemId = new byte[16];
                byte[] primeItemId2 = new byte[16];

                for (int i = 0; i < 16; i++) {
                    primeItemId[i] = response[i + 3];
                }*/

                /*
                if (primeItemId[0] == 0) {
                    inAlternativeItemId = true;

                    byte[] OptionalBlock = nfcV.transceive(cmd);


                    for (int k = 0; k < 16; k++) {
                        primeItemId2[k] = OptionalBlock[k + 5];
                    }
                } else {
                    for (int i = 0; i < primeItemId.length; i++) {
                        if (primeItemId[i] != 0) {
                            noId = true;
                        }
                    }
                }
/*
                if (noId){
                    payloadString= new String("NO ID");
                }else if (primeItemId2.length ==0){

                    ByteBuffer buffer = ByteBuffer.wrap(primeItemId);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int result = buffer.getShort();
                    payloadString= String.valueOf(result);

                }else {
                    byte[] newPrimeItemId= new byte[16*2];
                    for (int i=0;i<16; i++){
                        newPrimeItemId[i]= primeItemId[i];
                    }
                    for (int i=0;i<16; i++){
                        newPrimeItemId[i+16]= primeItemId[i];
                    }

                    ByteBuffer buffer = ByteBuffer.wrap(newPrimeItemId);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int result = buffer.getShort();
                    payloadString= String.valueOf(result);
                }*/

                nfcV.close();

            }catch (IOException ioException) {
                Toast.makeText(this, "Failed to read tag", Toast.LENGTH_LONG).show();

            }
        }
            return payloadString;

    }

}