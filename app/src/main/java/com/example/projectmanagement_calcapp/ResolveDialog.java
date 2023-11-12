package com.example.projectmanagement_calcapp;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ResolveDialog extends Dialog {

    float DIALOG_SIDE_MARGIN = 20f; //size in dp

    public ResolveDialog(final Context context, PositiveButtonListener onPositiveCallback){
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.resolve_dialog);

        Button cancel = findViewById(R.id.cancel);
        Button resolve = findViewById(R.id.resolve);
        TextView anchorId = findViewById(R.id.anchor_id);
        cancel.setOnClickListener(view ->
        {
            dismiss();
        });
        resolve.setOnClickListener(view ->
        {
            onPositiveCallback.onPositiveButtonClicked(anchorId.getText().toString());
            dismiss();
        });
    }

        interface PositiveButtonListener{
            void onPositiveButtonClicked(String dialogValue);
        }
}
