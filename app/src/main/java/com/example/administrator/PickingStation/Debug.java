package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;
import static com.example.administrator.PickingStation.Util.boolToString;
import static com.example.administrator.PickingStation.Util.inputToInt;

public class Debug extends Fragment {

    private OnFragmentInteractionListener mFragmentInteraction;
    private Boolean autoUpdate;
    private Button buttonRead;
    private Button buttonWrite;
    private Button buttonClear;
    private Button debug_encoder3000, armPosition, advanced;
    private TextView armDataOutput;
    private TextView commonDataOutput;
    private CheckBox checkBox_SBD, checkBox_MR, checkBox_SBFA;
    private CheckBox checkBox_SBFB, checkBox_BCRSA, checkBox_BCRSB;
    private CheckBox checkBox_MM, checkBox_VV, checkBox_CE;
    private CheckBox checkBox_TP, checkBox_ITT, checkBox_TMD;
    private EditText editText_MFB, editText_MLR, editText_MUD;
    private EditText editText_COD, editText_WTDWT, editText_PZA;
    private EditText editText_VOCD, editText_PCS, editText_ADD;
    private EditText editText_ZASV, editText_TPOX_AGB, editText_TPOX_AAD;

    private final Handler looperHandler = new Handler(Looper.getMainLooper());
    private int currentArm = 1;
    private int lastChecked_encoderValue;

    public Debug() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_debug, container, false);

        //UI elements
        ImageView debug_next = (ImageView) view.findViewById(R.id.debug_imageView_next);
        ImageView debug_previous = (ImageView) view.findViewById(R.id.debug_imageView_previous);
        buttonRead = (Button) view.findViewById(R.id.debug_read);
        buttonWrite = (Button) view.findViewById(R.id.debug_write);
        buttonClear = (Button) view.findViewById(R.id.debug_clear);
        debug_encoder3000 = (Button) view.findViewById(R.id.debug_encoder3000);
        armPosition = (Button) view.findViewById(R.id.debug_button_armPosition);
        advanced = (Button) view.findViewById(R.id.debug_button_advanced);
        commonDataOutput = (TextView) view.findViewById(R.id.debug_commonDataOutput);
        armDataOutput = (TextView) view.findViewById(R.id.debug_armDataOutput);
        final TextView currentArmText = (TextView) view.findViewById(R.id.debug_currentArm);
        final CheckBox autoUpdate = (CheckBox) view.findViewById(R.id.debug_checkBox_autoupdate);
        checkBox_SBD = (CheckBox) view.findViewById(R.id.debug_checkBox_SBD);
        checkBox_MR = (CheckBox) view.findViewById(R.id.debug_checkBox_MR);
        checkBox_SBFA = (CheckBox) view.findViewById(R.id.debug_checkBox_SBFA);
        checkBox_SBFB = (CheckBox) view.findViewById(R.id.debug_checkBox_SBFB);
        checkBox_BCRSA = (CheckBox) view.findViewById(R.id.debug_checkBox_BCRSA);
        checkBox_BCRSB = (CheckBox) view.findViewById(R.id.debug_checkBox_BCRSB);
        checkBox_MM = (CheckBox) view.findViewById(R.id.debug_checkBox_MM);
        checkBox_VV = (CheckBox) view.findViewById(R.id.debug_checkBox_VV);
        checkBox_CE = (CheckBox) view.findViewById(R.id.debug_checkBox_CE);
        checkBox_TP = (CheckBox) view.findViewById(R.id.debug_checkBox_TP);
        checkBox_ITT = (CheckBox) view.findViewById(R.id.debug_checkBox_ITT);
        checkBox_TMD = (CheckBox) view.findViewById(R.id.debug_checkBox_TMD);
        editText_MFB = (EditText) view.findViewById(R.id.debug_editText_MFB);
        editText_MLR = (EditText) view.findViewById(R.id.debug_editText_MLR);
        editText_MUD = (EditText) view.findViewById(R.id.debug_editText_MUD);
        editText_COD = (EditText) view.findViewById(R.id.debug_editText_COD);
        editText_WTDWT = (EditText) view.findViewById(R.id.debug_editText_WTDWT);
        editText_PZA = (EditText) view.findViewById(R.id.debug_editText_PZA);
        editText_VOCD = (EditText) view.findViewById(R.id.debug_editText_VOCD);
        editText_PCS = (EditText) view.findViewById(R.id.debug_editText_PCS);
        editText_ADD = (EditText) view.findViewById(R.id.debug_editText_ADD);
        editText_ZASV = (EditText) view.findViewById(R.id.debug_editText_ZASV);
        editText_TPOX_AGB = (EditText) view.findViewById(R.id.debug_editText_TPOX_AGB);
        editText_TPOX_AAD = (EditText) view.findViewById(R.id.debug_editText_TPOX_AAD);


        commonDataOutput.setMovementMethod(new ScrollingMovementMethod());
        armDataOutput.setMovementMethod(new ScrollingMovementMethod());

        buttonRead.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                requestDebugData();
            }
        });

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendDebugData();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                commonDataOutput.setText("");
                armDataOutput.setText("");
            }
        });

        debug_encoder3000.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editText_VOCD.setText(Integer.toString(lastChecked_encoderValue + 3000)); //cumple lo que promete, verdad?
            }
        });

        //Launch advanced settings window (manipulator position etc.)
        advanced.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchToLayout(R.id.opt_debug_advanced);
            }
        });

        debug_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentArm < 5) {
                    currentArm++;
                    currentArmText.setText(Integer.toString(currentArm));
                }
                requestDebugData();
            }
        });

        debug_previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentArm > 1) {
                    currentArm--;
                    currentArmText.setText(Integer.toString(currentArm));
                }
                requestDebugData();
            }
        });

        Runnable autoUpdater = new Runnable() {
            @Override
            public void run() {
                if (autoUpdate.isChecked()) {
                    requestDebugData();
                }
                looperHandler.postDelayed(this, 200);
            }
        };
        autoUpdater.run();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mFragmentInteraction = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteraction = null;
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand(String command);
    }

    private void requestDebugData() {
        try {
            JSONObject JSONOutput = new JSONObject();

            JSONOutput.put("command", "PGSI");
            JSONOutput.put("arm", currentArm);
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");

        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    public void updateDebugData(String mCmd) {
            if ((commonDataOutput != null) && (armDataOutput != null)) {
                try {
                    JSONObject JSONparser = new JSONObject(mCmd);
                    commonDataOutput.setText("");
                    armDataOutput.setText("");

                    armDataOutput.append("### Data for arm number " + Integer.valueOf(JSONparser.getInt("arm")).toString() + ":\n\n");
                    armDataOutput.append(">HasDischarged; " +  JSONparser.getBoolean("HasDischarged") + "\n");
                    armDataOutput.append(">PhotosensorOfManipulator; " +  JSONparser.getBoolean("PhotosensorOfManipulator") + "\n");
                    armDataOutput.append(">ManipulatorStatePosition; " + JSONparser.getBoolean("ManipulatorStatePosition") + "\n");
                    armDataOutput.append(">DischargedTheBrickConfirm; " + JSONparser.getBoolean("DischargedTheBrickConfirm") + "\n");
                    armDataOutput.append(">LeftStorageBinSecurity; " + JSONparser.getBoolean("LeftStorageBinSecurity") + "\n");
                    armDataOutput.append(">RightStorageBinSecurity; " + JSONparser.getBoolean("RightStorageBinSecurity") + "\n");
                    armDataOutput.append(">AlarmArray; " + Integer.valueOf(JSONparser.getInt("AlarmArray")).toString() + "\n");
                    armDataOutput.append(">ManipulatorRepositionState; " + Integer.valueOf(JSONparser.getInt("ManipulatorRepositionState")).toString() + "\n");
                    armDataOutput.append(">ActualValueEncoder; " + Integer.valueOf(JSONparser.getInt("ActualValueEncoder")).toString() + "\n\n");

                    commonDataOutput.append("### Common Data: \n\n");
                    commonDataOutput.append(">TheQueueOfPhotosensor_1; " + JSONparser.getBoolean("TheQueueOfPhotosensor_1") + "\n");
                    commonDataOutput.append(">TheQueueOfPhotosensor_2; " + JSONparser.getBoolean("TheQueueOfPhotosensor_2") + "\n");
                    commonDataOutput.append(">TheQueueOfPhotosensor_3; " + JSONparser.getBoolean("TheQueueOfPhotosensor_3") + "\n");
                    commonDataOutput.append(">TheQueueOfPhotosensor_4; " + JSONparser.getBoolean("TheQueueOfPhotosensor_4") + "\n");
                    commonDataOutput.append(">StationInterlock_16; " + JSONparser.getBoolean("StationInterlock_16") + "\n");
                    commonDataOutput.append(">WhetherOrNotPutTheTileTo_16; " + JSONparser.getBoolean("WhetherOrNotPutTheTileTo_16") + "\n");
                    commonDataOutput.append(">EquipmentAlarmArray; " + Integer.valueOf(JSONparser.getInt("EquipmentAlarmArray")).toString() + "\n");
                    commonDataOutput.append(">TileGrade; " + Integer.valueOf(JSONparser.getInt("TileGrade")).toString() + "\n");
                    commonDataOutput.append(">ChangeColor; " + Integer.valueOf(JSONparser.getInt("ChangeColor")).toString() + "\n");
                    commonDataOutput.append(">SystemState; " + Integer.valueOf(JSONparser.getInt("SystemState")).toString() + "\n");
                    commonDataOutput.append(">ActualValueOfTheLineEncoder; " + Integer.valueOf(JSONparser.getInt("ActualValueOfTheLineEncoder")).toString() + "\n");
                    commonDataOutput.append(">EnterTheTileStartingCodeValue; " + Integer.valueOf(JSONparser.getInt("EnterTheTileStartingCodeValue")).toString() + "\n");

                    lastChecked_encoderValue = Integer.valueOf(JSONparser.getInt("ActualValueOfTheLineEncoder"));
                } catch (Exception jsonExc) {
                    Log.e("JSON Exception", jsonExc.getMessage());
                }
            }
    }

    public void sendDebugData() {
        try {
            JSONObject JSONOutput = new JSONObject();

            JSONOutput.put("command", "PWDA");

            JSONOutput.put("arm", currentArm);
            JSONOutput.put("SBD", boolToString(checkBox_SBD.isChecked()));
            JSONOutput.put("MR", boolToString(checkBox_MR.isChecked()));
            JSONOutput.put("SBFA", boolToString(checkBox_SBFA.isChecked()));
            JSONOutput.put("SBFB", boolToString(checkBox_SBFB.isChecked()));
            JSONOutput.put("BCRSA", boolToString(checkBox_BCRSA.isChecked()));
            JSONOutput.put("BCRSB", boolToString(checkBox_BCRSB.isChecked()));
            JSONOutput.put("MM", boolToString(checkBox_MM.isChecked()));
            JSONOutput.put("VV", boolToString(checkBox_VV.isChecked()));
            JSONOutput.put("MFB", inputToInt(editText_MFB));
            JSONOutput.put("MLR", inputToInt(editText_MLR));
            JSONOutput.put("MUD", inputToInt(editText_MUD));
            JSONOutput.put("COD", inputToInt(editText_COD));
            JSONOutput.put("WTDWT", inputToInt(editText_WTDWT));
            JSONOutput.put("PZA", inputToInt(editText_PZA));
            JSONOutput.put("VOCD", inputToInt(editText_VOCD));
            JSONOutput.put("CE", boolToString(checkBox_CE.isChecked()));
            JSONOutput.put("TP", boolToString(checkBox_TP.isChecked()));
            JSONOutput.put("ITT", boolToString(checkBox_ITT.isChecked()));
            JSONOutput.put("TMD", boolToString(checkBox_TMD.isChecked()));
            JSONOutput.put("PCS", inputToInt(editText_PCS));
            JSONOutput.put("ADD", inputToInt(editText_ADD));
            JSONOutput.put("ZASV", inputToInt(editText_ZASV));
            JSONOutput.put("TPOX_AGB", inputToInt(editText_TPOX_AGB));
            JSONOutput.put("TPOX_AAD", inputToInt(editText_TPOX_AAD));

            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");

        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }


    public void startAutoUpdate() {
        autoUpdate = true;
    }

    public void stopAutoUpdate() {
        autoUpdate = false;
    }

}
