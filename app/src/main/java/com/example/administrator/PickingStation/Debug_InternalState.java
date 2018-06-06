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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class Debug_InternalState extends Fragment {


    private TextView height;
    private TextView availableDNI;
    private TextView BricksBefore;
    private TextView BricksOn;
    private TextView BricksReady;
    private TextView ManipulatorInfo;
    private JSONArray heightList;
    private JSONArray availableDNIList;
    private JSONArray bricksBeforeLineList;
    private JSONArray bricksOnLineList;
    private JSONArray BricksReadyForOutputList;
    private JSONArray manipulatorOrderList;
    private JSONArray manipulatorModeList;
    private JSONArray manipulatorPositionList;
    private JSONArray manipulatorTakenBricks;

    public Debug_InternalState() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.debug_internal_variables, container, false);

        height = (TextView) view.findViewById(R.id.internal_height);
        availableDNI = (TextView) view.findViewById(R.id.internal_DNIlist);
        BricksBefore = (TextView) view.findViewById(R.id.internal_BricksBefore);
        BricksOn = (TextView) view.findViewById(R.id.internal_BricksOn);
        BricksReady = (TextView) view.findViewById(R.id.internal_BricksReady);
        ManipulatorInfo = (TextView) view.findViewById(R.id.internal_ManipulatorInfo);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

                //Delete old data
                height.setText("");
                availableDNI.setText("");
                BricksBefore.setText("");
                BricksOn.setText("");
                BricksReady.setText("");
                ManipulatorInfo.setText("");

                //TODO Print new data



        } catch(JSONException exc) {
            Log.d("JSON exception", "Can't parse Internal State Debug Data string");
        }
    }



}
