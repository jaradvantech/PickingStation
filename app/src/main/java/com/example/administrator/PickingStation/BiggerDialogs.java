package com.example.administrator.PickingStation;

/*
 * RBS
 * The default text size for dialogs is too small for this tablet.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.TextView;

public class BiggerDialogs {

    static void show(final AlertDialog mDialog) {
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(Dialog.BUTTON_POSITIVE).setTextSize(18);
                mDialog.getButton(Dialog.BUTTON_NEUTRAL).setTextSize(18);
                mDialog.getButton(Dialog.BUTTON_NEGATIVE).setTextSize(18);
            }
        });
        mDialog.show();
        TextView textView = (TextView) mDialog.findViewById(android.R.id.message);
        textView.setTextSize(18);
    }
}
