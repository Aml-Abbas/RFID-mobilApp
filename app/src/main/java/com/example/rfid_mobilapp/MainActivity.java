package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MainActivity extends AppCompatActivity {
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagContentTextView = findViewById(R.id.tagContentTextView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;

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
        String payload = getPayload(intent);
        tagContentTextView.setText(payload);

        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://aml-abbas.github.io/RFID-Pages/"));
        Intent chooser = Intent.createChooser(intent, "Open webbsite for item:" + payload);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

    private String getPayload(Intent intent) {
        String payloadString = "test";

        if (intent != null) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            NfcV nfcV = NfcV.get(tag);

            boolean inAlternativeItemId = false;
            boolean noId = true;

            try {
                nfcV.connect();

                byte[] tagId = tag.getId();



                byte[] response = nfcV.transceive(getCommand(tagId, 0,0));

                byte[] primeItemId = new byte[16];
                byte[] primeItemId2 = new byte[16];

                System.out.println("the respons size is: " + response.length);


                for (int i = 0; i < 6; i++) {
                    primeItemId[i] = response[i + 3];
                }


                if (primeItemId[0] == 1) {
                    inAlternativeItemId = true;

                    byte[] OptionalBlock = nfcV.transceive(getCommand(tagId, 32, 0));


                    for (int k = 0; k < 16; k++) {
                        primeItemId2[k] = OptionalBlock[k + 5];
                    }
                    noId = false;
                } else {
                    for (int i = 0; i < primeItemId.length; i++) {
                        if (primeItemId[i] != 0) {
                            noId = false;
                        }
                    }
                }

                if (noId) {
                    payloadString = new String("NO ID");
                } else if (primeItemId2.length == 0) {

                    ByteBuffer buffer = ByteBuffer.wrap(primeItemId);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int result = buffer.getShort();
                    payloadString = String.valueOf(result);

                } else {
                    byte[] newPrimeItemId = new byte[16 * 2];
                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i] = primeItemId[i];
                    }
                    for (int i = 0; i < 16; i++) {
                        newPrimeItemId[i + 16] = primeItemId[i];
                    }

                    ByteBuffer buffer = ByteBuffer.wrap(newPrimeItemId);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                    int result = buffer.getShort();
                    payloadString = String.valueOf(result);
                }


                nfcV.close();

            } catch (IOException ioException) {
                Toast.makeText(this, "Failed to read tag", Toast.LENGTH_LONG).show();

            }
        }
        return payloadString;

    }

    private byte[] getCommand(byte[] tagId, int offset, int blocks){

        byte[] cmd = new byte[]{
                (byte) 0x60,  // flags: addressed (= UID field present)
                (byte) 0x23, // command: READ MULTIPLE BLOCKS
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,  // placeholder for tag UID
                (byte) (offset & 0x0ff),  // first block number
                (byte) ((blocks - 1) & 0x0ff)  // number of blocks (-1 as 0x00 means one block)
        };
        System.arraycopy(tagId, 0, cmd, 2, 8);
        return cmd;
    }

}