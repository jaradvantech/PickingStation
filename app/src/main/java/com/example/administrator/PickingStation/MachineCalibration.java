package com.example.administrator.PickingStation;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MachineCalibration extends Fragment {

    private View view;
    private int MANIPULATORS = -1;
    private OnFragmentInteractionListener mFragmentInteraction;
    private ArrayList<EditText> textFields;
    private ArrayList<TextView> textViews;
    private Button setManipulators, send, startStop, advance, microAdvance;
    private EditText manipulatorNum, standByValue, waitingPosition;
    private Boolean motorRuning = false;
    private int encoderIncrement = 0,  currentStep = 0;
    private final int ADVANCE_PERIOD = 300;
    private final int MICROADVANCE_PERIOD = 70;

    /*
     * RBS, 20th June, 2018;
     * We are in a rush: for the moment, this fragment is prepared
     * to work with up to 10 manipulators. In the unlikely event, of 爱而生 making
     * a picking station with more than 10, this will be rewritten to use dynamic content.
     *
     * A more advanced version will include automatic calculation of the points.
     */

    public MachineCalibration() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calibration, container, false);

        textFields = new ArrayList<>();
        textViews = new ArrayList<>();
        for (int i=0; i<10; i++) {
            textFields.add((EditText) view.findViewById(getResources().getIdentifier("calibration_edittext_arm" + (i + 1), "id", getActivity().getPackageName())));
            textViews.add((TextView) view.findViewById(getResources().getIdentifier("calibration_textview_arm" + (i + 1), "id", getActivity().getPackageName())));
        }

        setManipulators = view.findViewById(R.id.calibration_button_setManipulators);
        send = view.findViewById(R.id.calibration_button_send);
        startStop = view.findViewById(R.id.calibration_button_motorRun);
        advance = view.findViewById(R.id.calibration_button_advance);
        microAdvance = view.findViewById(R.id.calibration_button_microAdvance);
        manipulatorNum = view.findViewById(R.id.calibration_editText_totalManipulators);
        standByValue =  view.findViewById(R.id.calibration_editText_tSBV);
        waitingPosition = view.findViewById(R.id.calibration_editText_AGBTTWPIA);

        setManipulators.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                int fieldValue = Util.inputToInt(manipulatorNum);
                if(fieldValue > 0) {
                    setManipulators(fieldValue);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                sendParameters();
            }
        });

        startStop.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                    setLineMotor(!motorRuning); //toggle status
            }
        });

        advance.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                setLineMotor(true);
                Handler handlerAdvance = new Handler();
                handlerAdvance.postDelayed(new Runnable() {
                    public void run() {
                        setLineMotor(false);
                    }
                }, ADVANCE_PERIOD);
            }
        });

        microAdvance.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                setLineMotor(true);
                Handler handlerMicroAdvance = new Handler();
                handlerMicroAdvance.postDelayed(new Runnable() {
                    public void run() {
                        setLineMotor(false);
                    }
                }, MICROADVANCE_PERIOD);
            }
        });

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

    private void setManipulators(int mMANIPULATORS) {
        if(mMANIPULATORS <= 10) {
            this.MANIPULATORS = mMANIPULATORS;

            //Set visible only as many as required
            setAllInvisible();
            for (int i=0; i<MANIPULATORS; i++) {
                textFields.get(i).setVisibility(View.VISIBLE);
                textViews.get(i).setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getActivity().getBaseContext(),"Too many...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAllInvisible() {
        for (int i=0; i<10; i++) {
            textFields.get(i).setVisibility(View.INVISIBLE);
            textViews.get(i).setVisibility(View.INVISIBLE);
        }
    }

    private void sendParameters() {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONArray positionArray = new JSONArray();
            JSONOutput.put("command_ID", "SCAP");
            JSONOutput.put("armNumber", MANIPULATORS);
            JSONOutput.put("positions", positionArray);
            for(int i=0; i<MANIPULATORS; i++) {
                positionArray.put(Integer.parseInt(textFields.get(i).getText().toString()));
            }
            JSONOutput.put("SBV", Util.inputToInt(standByValue));
            JSONOutput.put("AGBTTWPIA", Util.inputToInt(waitingPosition));
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");

            //Save to local config
            SettingManager.setTotalManipulators(MANIPULATORS);
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    public void setLineMotor(Boolean state) {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("command_ID", "PWDA");
            if(state == true) {
            /*Start the line*/
                JSONOutput.put("TMD", true);
                JSONOutput.put("PCS", 3);
                startStop.setText("STOP");
                motorRuning = true;
            } else {
            /*Stop the line*/
                JSONOutput.put("TMD", false);
                JSONOutput.put("PCS", 1);
                startStop.setText("START");
                motorRuning = false;
            }
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }
}
