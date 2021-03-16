package com.example.rfid_mobilapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class TagProgressDialog extends AppCompatDialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_layout);
        builder.setTitle(R.string.welcome)
                .setMessage(R.string.place_tag)

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        closeApp(getContext());
                    }
                });
        return builder.create();
    }

    private void closeApp( Context context) {
        ((Activity)context).finish();
    }
}