package com.example.administrator.PickingStation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Util {

    public static String inputToString(EditText mUserInput, String format) {
        //Get int from input
        String inputText = mUserInput.getText().toString();
        //default is zero
        if (TextUtils.isEmpty(inputText))
            inputText = "0";
        int inputNumber = Integer.parseInt(inputText);
        //Format int into string
        return String.format(format, inputNumber);
    }

    public static int inputToInt(EditText mUserInput) {
        String inputText = mUserInput.getText().toString();
        if (TextUtils.isEmpty(inputText))
            inputText = "0";
        return Integer.parseInt(inputText);
    }

    public static String boolToString(Boolean mBol) {
        if (mBol)
            return "1";
        else
            return "0";
    }

    public static Boolean charToBoolean(char c) {
        if (c == '0')
            return true;
        else
            return false;
    }

    public static String inetToJSON(String ip, String port) {
        String retval = "";
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("ip", ip);
            JSONOutput.put("port", port);
            retval = JSONOutput.toString();
        } catch (JSONException exc) {
        }
        return retval;
    }
}
