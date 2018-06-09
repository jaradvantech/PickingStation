package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class DebugAdvancedOptions extends Fragment {

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
    private TextView height;
    private TextView BricksOn;
    private TextView BricksReady;
    private TextView ManipulatorInfo;
    private TextView takenBricks;
    private TextView miscInformation;
    private JSONArray heightList;
    private JSONArray availableDNIList;
    private JSONArray bricksBeforeLineList;
    private JSONArray manipulatorState;
    private JSONArray bricksOnLineList;
    private JSONArray BricksReadyForOutputList;
    private JSONArray manipulatorOrderList;
    private JSONArray manipulatorModeList;
    private JSONArray manipulatorPositionList;
    private JSONArray manipulatorTakenBricks;
    private Boolean autoUpdate = false;
    private Runnable autoUpdater;
    private Manual.OnFragmentInteractionListener mFragmentInteraction;
    private final Handler looperHandler = new Handler(Looper.getMainLooper());

    public DebugAdvancedOptions() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debugadvancedoptions, container, false);

        button_placeorder = (Button) view.findViewById(R.id.presets_button_placeorder);
        button_send = (Button) view.findViewById(R.id.presets_button_send);
        button_forcepallet = (Button) view.findViewById(R.id.presets_button_forcepallet);
        presets_editText_what = (EditText) view.findViewById(R.id.presets_editText_what);
        presets_editText_when = (EditText) view.findViewById(R.id.presets_editText_when);
        presets_editText_where = (EditText) view.findViewById(R.id.presets_editText_where);
        editText_forcetopallet = (EditText) view.findViewById(R.id.presets_editText_forcetopallet);
        switch_enable16 = (Switch) view.findViewById(R.id.advanced_switch_enable16);
        switch_forceOutput = (Switch) view.findViewById(R.id.advanced_switch_forceoutput);
        switch_forceInput = (Switch) view.findViewById(R.id.advanced_switch_forceinput);
        height = (TextView) view.findViewById(R.id.advanced_height);
        BricksOn = (TextView) view.findViewById(R.id.advanced_BricksOn);
        BricksReady = (TextView) view.findViewById(R.id.advanced_BricksReady);
        ManipulatorInfo = (TextView) view.findViewById(R.id.advanced_ManipulatorInfo);
        takenBricks = (TextView) view.findViewById(R.id.advanced_takenBricks);
        miscInformation = (TextView) view.findViewById(R.id.advanced_misc);

        //Enable scroll on textviews
        height.setMovementMethod(new ScrollingMovementMethod());
        BricksOn.setMovementMethod(new ScrollingMovementMethod());
        BricksReady.setMovementMethod(new ScrollingMovementMethod());
        ManipulatorInfo.setMovementMethod(new ScrollingMovementMethod());
        takenBricks.setMovementMethod(new ScrollingMovementMethod());

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

        //debug
        //String testString = "{\"command_ID\":\"GDIS\",\"Available_DNI_List\":[2,3,4,5,6,7,8,9,10],\"Manipulator_Order_List\":[{},{},{},{\"What\":false,\"When\":1058,\"Where\":false},{}],\"Bricks_Before_The_Line\":[],\"Bricks_On_The_Line\":[{\"DNI\":1,\"AssignedPallet\":8,\"Position\":7842,\"Type\":17},{\"DNI\":0,\"AssignedPallet\":0,\"Position\":5642,\"Type\":0}],\"Bricks_Ready_For_Output\":[0,0,0,0,0,0,0,0,0,0],\"Manipulator_Fixed_Position\":[2900,5000,7000,8900,10800],\"Manipulator_Modes\":[1,1,1,1,1],\"Pallet_LowSpeedPulse_Height_List\":[5000,5000,5000,5000,5000,5000,5000,5000,5000,5000],\"Manipulator_TakenBrick\":[{\"DNI\":0,\"AssignedPallet\":0,\"Position\":0,\"Type\":1},{\"DNI\":0,\"AssignedPallet\":0,\"Position\":0,\"Type\":1},{\"DNI\":0,\"AssignedPallet\":0,\"Position\":0,\"Type\":1},{\"DNI\":0,\"AssignedPallet\":0,\"Position\":0,\"Type\":1},{\"DNI\":0,\"AssignedPallet\":0,\"Position\":0,\"Type\":1}],\"Manipulator_State\":[{\"WhatToDoWithTheBrick\":0,\"CatchOrDrop\":0,\"ValueOfCatchDrop\":0},{\"WhatToDoWithTheBrick\":0,\"CatchOrDrop\":0,\"ValueOfCatchDrop\":0},{\"WhatToDoWithTheBrick\":0,\"CatchOrDrop\":0,\"ValueOfCatchDrop\":0},{\"WhatToDoWithTheBrick\":0,\"CatchOrDrop\":0,\"ValueOfCatchDrop\":0},{\"WhatToDoWithTheBrick\":0,\"CatchOrDrop\":0,\"ValueOfCatchDrop\":0}]}";
       // this.parseInternalStateDebugData(testString);

        autoUpdater = new Runnable() {
            @Override
            public void run() {
                if (autoUpdate) {
                    mFragmentInteraction.onSendCommand("{\"command_ID\":\"GDIS\"}");
                }
                looperHandler.postDelayed(this, 200);
            }
        };
        autoUpdater.run();

        return view;
    }


    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof Manual.OnFragmentInteractionListener) {
            mFragmentInteraction = (Manual.OnFragmentInteractionListener) context;
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

    public void parseInternalStateDebugData(String debugData) {
        try {
            JSONObject JSONparser = new JSONObject(debugData);

            heightList = JSONparser.getJSONArray("Pallet_LowSpeedPulse_Height_List");
            availableDNIList = JSONparser.getJSONArray("Available_DNI_List");
            bricksBeforeLineList = JSONparser.getJSONArray("Bricks_Before_The_Line");
            bricksOnLineList = JSONparser.getJSONArray("Bricks_On_The_Line");
            BricksReadyForOutputList = JSONparser.getJSONArray("Bricks_Ready_For_Output");
            manipulatorOrderList = JSONparser.getJSONArray("Manipulator_Order_List");
            manipulatorModeList = JSONparser.getJSONArray("Manipulator_Modes");
            manipulatorPositionList = JSONparser.getJSONArray("Manipulator_Fixed_Position");
            manipulatorTakenBricks = JSONparser.getJSONArray("Manipulator_TakenBrick");
            manipulatorState = JSONparser.getJSONArray("Manipulator_State");

            //Delete old data
            takenBricks.setText("");
            height.setText("");
            BricksOn.setText("");
            BricksReady.setText("");
            ManipulatorInfo.setText("");

            //Fill taken bricks
            takenBricks.append("Taken Bricks:\n");
            for(int i=0; i<manipulatorTakenBricks.length(); i++) {
                takenBricks.append("\nManipulator " + i + ":\n");
                takenBricks.append("DNI: " + Integer.valueOf(manipulatorTakenBricks.getJSONObject(i).getInt("DNI")).toString() + "\n");
                takenBricks.append("Assigned pallet: " + Integer.valueOf(manipulatorTakenBricks.getJSONObject(i).getInt("AssignedPallet")).toString() + "\n");
                takenBricks.append("Position: " + Integer.valueOf(manipulatorTakenBricks.getJSONObject(i).getInt("Position")).toString() + "\n");
                takenBricks.append("Type: " + Integer.valueOf(manipulatorTakenBricks.getJSONObject(i).getInt("Type")).toString() + "\n");
            }

            //Fill Orders
            ManipulatorInfo.append("Orders:\n");
            for(int i=0; i<manipulatorOrderList.length(); i++) {
                if(manipulatorOrderList.getJSONObject(i).has("What") &&
                        manipulatorOrderList.getJSONObject(i).has("When") &&
                        manipulatorOrderList.getJSONObject(i).has("Where")) {

                    ManipulatorInfo.append("Arm #" + (i+1) + "-->  ");
                    ManipulatorInfo.append("What: " + Boolean.valueOf(manipulatorOrderList.getJSONObject(i).getBoolean("What")).toString() + ", ");
                    ManipulatorInfo.append("When: " + Integer.valueOf(manipulatorOrderList.getJSONObject(i).getInt("When")).toString() + ", ");
                    ManipulatorInfo.append("Where: " + Boolean.valueOf(manipulatorOrderList.getJSONObject(i).getBoolean("Where")).toString() + "\n");
                }
            }

            //Fill Manipulator state
            ManipulatorInfo.append("\nState:\n");
            for(int i=0; i<manipulatorState.length(); i++) {
                ManipulatorInfo.append("Arm #" + i + ":  ");
                ManipulatorInfo.append("WhatToDo: " + Integer.valueOf(manipulatorState.getJSONObject(i).getInt("WhatToDoWithTheBrick")).toString() + "   ");
                ManipulatorInfo.append("Catch/Drop: " + Integer.valueOf(manipulatorState.getJSONObject(i).getInt("CatchOrDrop")).toString() + "   ");
                ManipulatorInfo.append("Value c/d: " + Integer.valueOf(manipulatorState.getJSONObject(i).getInt("ValueOfCatchDrop")).toString() + "\n");
            }


            //Fill Bricks On the line
            BricksOn.append("Bricks on the line:\n");
            for(int i=0; i<bricksOnLineList.length(); i++) {
                BricksOn.append("\n");
                BricksOn.append("DNI: " + Integer.valueOf(bricksOnLineList.getJSONObject(i).getInt("DNI")).toString() + "\n");
                BricksOn.append("Assigned pallet: " + Integer.valueOf(bricksOnLineList.getJSONObject(i).getInt("AssignedPallet")).toString() + "\n");
                BricksOn.append("Position: " + Integer.valueOf(bricksOnLineList.getJSONObject(i).getInt("Position")).toString() + "\n");
                BricksOn.append("Type: " + Integer.valueOf(bricksOnLineList.getJSONObject(i).getInt("Type")).toString() + "\n\n");
            }
            BricksOn.append("\n[Available DNIs]\n");
            for(int i=0; i<availableDNIList.length(); i++) {
                BricksOn.append(availableDNIList.get(i).toString() + " ");
            }

            //Fill Bricks ready for output
            BricksReady.append("Ready for output:\n");
            for(int i=0; i<BricksReadyForOutputList.length(); i++) {
                BricksReady.append("Pallet " + (i+1) + ": ");
                BricksReady.append(BricksReadyForOutputList.get(i).toString());
                BricksReady.append("\n");
            }

            //Fill pallet heights
            height.append("Heights:\n");
            for(int i=0; i<heightList.length(); i++) {
                height.append("Pallet " + (i+1) + ": ");
                height.append(heightList.get(i).toString());
                height.append("\n");
            }

            //Other information
            //Fill modes
            miscInformation.append("Modes:\n");
            for(int i=0; i<manipulatorModeList.length(); i++) {
                miscInformation.append(manipulatorModeList.get(i).toString() + "  ");
            }
            //Fill Positions
            miscInformation.append("\n\nFixed Positions:\n");
            for(int i=0; i<manipulatorPositionList.length(); i++) {
                miscInformation.append(manipulatorPositionList.get(i).toString() + "\n");
            }


        } catch(JSONException exc) {
            Log.d("JSON exception", "Can't parse Internal State Debug Data string. " + exc.getMessage().toString());
        }
    }


    private void generateFOTP() {
        String command = "FOTP_";

        command += inputToString(editText_forcetopallet, "%02d");
        command += "\r\n";

        mFragmentInteraction.onSendCommand(command);
    }

    private void generateWADI() {
        String command = "WADI_";

        command += boolToString(switch_enable16.isChecked());
        command += boolToString(switch_forceOutput.isChecked());
        command += boolToString(switch_forceInput.isChecked());
        command += "\r\n";

        mFragmentInteraction.onSendCommand(command);
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

        mFragmentInteraction.onSendCommand(command);
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

    public void startAutoUpdate() {
        autoUpdate = true;
    }

    public void stopAutoUpdate() {
        autoUpdate = false;
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }
}
