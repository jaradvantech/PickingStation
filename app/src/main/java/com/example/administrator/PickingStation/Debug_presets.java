package com.example.administrator.PickingStation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class Debug_presets extends AppCompatActivity {

    private EditText presets_editText_what;
    private EditText presets_editText_when;
    private EditText presets_editText_where;
    private EditText editText_forcetopallet;
    private Button button_forcepallet;
    private Button button_placeorder;
    private Button button_send;
    private Switch switch_enable16;
    private Switch switch_forceOutput;
    private Switch switch_forceInput;

    private boolean _16isenabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_presets);
        setTitle("Advanced Settings");

        button_placeorder = (Button) this.findViewById(R.id.presets_button_placeorder);
        button_send = (Button) this.findViewById(R.id.presets_button_send);
        button_forcepallet = (Button) this.findViewById(R.id.presets_button_forcepallet);
        presets_editText_what = (EditText) this.findViewById(R.id.presets_editText_what);
        presets_editText_when = (EditText) this.findViewById(R.id.presets_editText_when);
        presets_editText_where = (EditText) this.findViewById(R.id.presets_editText_where);
        editText_forcetopallet = (EditText) this.findViewById(R.id.presets_editText_forcetopallet);
        switch_enable16 = (Switch) this.findViewById(R.id.advanced_switch_enable16);
        switch_forceOutput = (Switch) this.findViewById(R.id.advanced_switch_forceoutput);
        switch_forceInput = (Switch) this.findViewById(R.id.advanced_switch_forceinput);

        //default
        switch_enable16.setChecked(true);
        switch_forceOutput.setVisibility(View.INVISIBLE);
        switch_forceInput.setVisibility(View.INVISIBLE);

        button_placeorder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generatePLRD();
            }
        });

        button_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateWADI();
            }
        });

        button_forcepallet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateFOTP();
            }
        });

        switch_enable16.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switch_forceOutput.setVisibility(View.INVISIBLE);
                    switch_forceInput.setVisibility(View.INVISIBLE);
                } else {
                    switch_forceOutput.setVisibility(View.VISIBLE);
                    switch_forceInput.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void generateFOTP() {
        String command = "FOTP_";

        command += inputToString(editText_forcetopallet, "%02d");
        command += "\r\n";

        Intent intent = new Intent();
        intent.putExtra("CMDstring", command);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void generateWADI() {
        String command = "WADI_";

        command += boolToString(switch_enable16.isChecked());
        command += boolToString(switch_forceOutput.isChecked());
        command += boolToString(switch_forceInput.isChecked());
        command += "\r\n";

        Intent intent = new Intent();
        intent.putExtra("CMDstring", command);
        setResult(RESULT_OK, intent);
        finish();
    }

    //PLace oRDer
    private void generatePLRD(){
        String command = "PLRD_";

        command += inputToString(presets_editText_what, "%06d");
        command += "_";
        command += inputToString(presets_editText_when, "%06d");
        command += "_";
        command += inputToString(presets_editText_where, "%06d");
        command += "\r\n";


        Intent intent = new Intent();
        intent.putExtra("CMDstring", command);
        setResult(RESULT_OK, intent);
        finish();
    }

    String inputToString(EditText mUserInput, String format) {
        //Get int from input
        String inputText = mUserInput.getText().toString();
        //default is zero
        if(TextUtils.isEmpty(inputText))
            inputText = "0";
        int inputNumber = Integer.parseInt(inputText);
        //Format int into string
        return String.format(format, inputNumber);
    }

    private String boolToString(Boolean mBol) {
        if (mBol)
            return "1";
        else
            return "0";
    }
}
