package com.example.administrator.PickingStation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import static com.example.administrator.PickingStation.Util.inputToInt;
import static com.example.administrator.PickingStation.Util.inputToString;

public class AskForIP extends AppCompatActivity {

    private EditText field1, field2, field3, field4, port;
    private ImageView goButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_askforip);

        field1 = (EditText) this.findViewById(R.id.askforip_editText_field1);
        field2 = (EditText) this.findViewById(R.id.askforip_editText_field2);
        field3 = (EditText) this.findViewById(R.id.askforip_editText_field3);
        field4 = (EditText) this.findViewById(R.id.askforip_editText_field4);
        port = (EditText) this.findViewById(R.id.askforip_editText_port);
        goButton = (ImageView) this.findViewById(R.id.askforip_imageView_go);

        //set defaults
        Intent intent = getIntent();
        String Server_IP_split[] = intent.getStringExtra("defaultIP").split("[.]+");
        if (Server_IP_split.length == 4 ) {
            field1.setText(Server_IP_split[0]);
            field2.setText(Server_IP_split[1]);
            field3.setText(Server_IP_split[2]);
            //field4.setText(Server_IP_split[3]); //Do not prefill this field, as this is what most people will change.
            port.setText(intent.getStringExtra("defaultPort"));
        }
        goButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });
    }

    private void save() {
        if(fieldsAreValid()) {
            String string_ip, string_port;
            string_ip   = inputToString(field1, "%03d") + ".";
            string_ip += inputToString(field2, "%03d") + ".";
            string_ip += inputToString(field3, "%03d") + ".";
            string_ip += inputToString(field4, "%03d");
            string_port = inputToString(port, "%05d");

            Intent intent = new Intent();
            intent.putExtra("ip", string_ip);
            intent.putExtra("port", string_port);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private boolean fieldsAreValid() {
        boolean retBool = true;
        field1.setTextColor(Color.parseColor("#000000"));
        field2.setTextColor(Color.parseColor("#000000"));
        field3.setTextColor(Color.parseColor("#000000"));
        field4.setTextColor(Color.parseColor("#000000"));
        port.setTextColor(Color.parseColor("#000000"));
        if(inputToInt(field1) >= 255) {
            retBool = false;
            field1.setTextColor(Color.parseColor("#ff0d00"));
        }
        if(inputToInt(field2) >= 255) {
            retBool = false;
            field2.setTextColor(Color.parseColor("#ff0d00"));
        }
        if(inputToInt(field3) >= 255) {
            retBool = false;
            field3.setTextColor(Color.parseColor("#ff0d00"));
        }
        if(inputToInt(field4) > 254 || inputToInt(field4) == 0) {
            retBool = false;
            field4.setTextColor(Color.parseColor("#ff0d00"));
        }
        if(inputToInt(port) <= 100 || inputToInt(port) >= 65535) {
            retBool = false;
            port.setTextColor(Color.parseColor("#ff0d00"));
        }
        return  retBool;
    }
}
