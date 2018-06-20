package com.example.administrator.PickingStation;

import android.text.TextUtils;
import android.widget.EditText;

public class Util {

    public static String inputToString(EditText mUserInput, String format) {
        //Get int from input
        String inputText = mUserInput.getText().toString();
        //default is zero
        if(TextUtils.isEmpty(inputText))
            inputText = "0";
        int inputNumber = Integer.parseInt(inputText);
        //Format int into string
        return String.format(format, inputNumber);
    }

    public static int inputToInt(EditText mUserInput) {
        String inputText = mUserInput.getText().toString();
        if(TextUtils.isEmpty(inputText))
            inputText = "0";
        return Integer.parseInt(inputText);
    }

    public static String boolToString(Boolean mBol) {
        if (mBol)
            return "1";
        else
            return "0";
    }

    public static Boolean charToBoolean(char c)
    {
        if(c=='0')
            return true;
        else
            return false;
    }
}
