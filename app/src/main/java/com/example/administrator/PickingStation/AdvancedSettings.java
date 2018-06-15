package com.example.administrator.PickingStation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.administrator.PickingStation.Util.inputToString;

public class AdvancedSettings extends AppCompatActivity {

    private EditText advanced_edittext_arm1,
                advanced_edittext_arm2,
                advanced_edittext_arm3,
                advanced_edittext_arm4,
                advanced_edittext_arm5;

    private Button advanced_button_arm1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);
        setTitle("Advanced Settings");

        advanced_button_arm1 = (Button) this.findViewById(R.id.advanced_button_arm1);
        advanced_edittext_arm1 = (EditText) this.findViewById(R.id.advanced_edittext_arm1);
        advanced_edittext_arm2 = (EditText) this.findViewById(R.id.advanced_edittext_arm2);
        advanced_edittext_arm3 = (EditText) this.findViewById(R.id.advanced_edittext_arm3);
        advanced_edittext_arm4 = (EditText) this.findViewById(R.id.advanced_edittext_arm4);
        advanced_edittext_arm5 = (EditText) this.findViewById(R.id.advanced_edittext_arm5);

        advanced_button_arm1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                generateCMD();
            }
        });
    }

    private void generateCMD(){
        String command = "SCAP_05_";
        command += inputToString(advanced_edittext_arm1, "%06d");
        command += "_";
        command += inputToString(advanced_edittext_arm2, "%06d");
        command += "_";
        command += inputToString(advanced_edittext_arm3, "%06d");
        command += "_";
        command += inputToString(advanced_edittext_arm4, "%06d");
        command += "_";
        command += inputToString(advanced_edittext_arm5, "%06d");

        Intent intent = new Intent();
        intent.putExtra("CMDstring", command);
        setResult(RESULT_OK, intent);
        finish();
    }
}
