package com.example.rfid_mobilapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    TextView tagContentTextView;
    NfcAdapter mNfcAdapter;
    Context mainActivityContext;
    String newItemId;
    String checkValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tagContentTextView = findViewById(R.id.tagContentTextView);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mainActivityContext = this;

        Intent intent = getIntent();
        newItemId="";
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String itemId = uri.getQueryParameter("itemid");
            tagContentTextView.setText("tag's new itemId: " + itemId);
            newItemId= itemId;
            checkValue = uri.getQueryParameter("checkValue");
        }
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
        if (checkValue!= null){
            if (checkValue.equals("false")){
                NfcTagUtil.check(intent, this, false);
            }else {
                NfcTagUtil.check(intent, this, true);
            }
        }else if(newItemId!= ""){
            NfcTagUtil.writeNewItemId(newItemId, intent, this);
            newItemId="";
        }else {
            tagContentTextView.setText("");
            String payload = NfcTagUtil.getItemId(intent, this);
            tagContentTextView.setText(payload);

            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://aml-abbas.github.io/rfid-pages/?itemId=" + payload));
            Intent chooser = Intent.createChooser(intent, "Open website for item:" + payload);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }

    }

}