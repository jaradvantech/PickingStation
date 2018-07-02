package com.example.administrator.PickingStation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import static com.example.administrator.PickingStation.Util.inputToInt;
import static com.example.administrator.PickingStation.Util.inputToString;

public class LogInActivity extends AppCompatActivity {

    private EditText password;
    private ImageView goButton;
    private final String IF_YOU_DECOMPILED_THIS = "8";
    private final String TO_FIND_OUT_THE_PASSWORD = "8";
    private final String YOU_ARE_COMPETENT_ENOUGH = "8";
    private final String TO_RUN_THE_CALIBRATION = "8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        password = (EditText) this.findViewById(R.id.login_editText_password);
        goButton = (ImageView) this.findViewById(R.id.login_imageView_go);

        goButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkPassword();
            }
        });
    }

    private void checkPassword() {
            Intent intent = new Intent();
            if(password.getText().toString().equals(
                    IF_YOU_DECOMPILED_THIS +
                    TO_FIND_OUT_THE_PASSWORD +
                    YOU_ARE_COMPETENT_ENOUGH +
                    TO_RUN_THE_CALIBRATION
            ))
                intent.putExtra("authentication", "ok");
            else
                intent.putExtra("authentication", "nok");
            setResult(RESULT_OK, intent);
            finish();
    }

}
