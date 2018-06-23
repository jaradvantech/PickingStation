package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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

import static com.example.administrator.PickingStation.Util.inputToInt;


public class MachineCalibration extends Fragment {

    private View view;
    private int MANIPULATORS = -1;
    private OnFragmentInteractionListener mFragmentInteraction;
    private ArrayList<EditText> textFields;
    private ArrayList<TextView> textViews;
    private Button setManipulators, send, startStop, advance, microAdvance;
    private EditText manipulatorNum;
    private Boolean motorRuning = false;
    private int encoderIncrement = 0;
    private final int ADVANCE_PERIOD = 500;
    private final int MICROADVANCE_PERIOD = 100;

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

        setManipulators = (Button) view.findViewById(R.id.calibration_button_setManipulators);
        send = (Button) view.findViewById(R.id.calibration_button_send);
        startStop = (Button) view.findViewById(R.id.calibration_button_motorRun);
        advance = (Button) view.findViewById(R.id.calibration_button_advance);
        microAdvance = (Button) view.findViewById(R.id.calibration_button_microAdvance);
        manipulatorNum = (EditText) view.findViewById(R.id.calibration_editText_totalManipulators);

        setManipulators.setOnClickListener(new View.OnClickListener() {
            public void onClick( View v ) {
                if(MANIPULATORS > 0) {
                    setManipulators(Integer.parseInt(manipulatorNum.getText().toString()));
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
            JSONOutput.put("totalArms", MANIPULATORS);
            for(int i=0; i<MANIPULATORS; i++) {
                positionArray.put(Integer.parseInt(textViews.get(i).getText().toString()));
            }
            JSONOutput.put("positions", positionArray);
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
            saveParameters();
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    /*
     * Save to local config as well
     */
    private void saveParameters() {

    }

    public void setLineMotor(Boolean state) {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("command_ID", "PWDA");
            if(state == true) {
            /*Start the line*/
                JSONOutput.put("TransmissionManualDebugging", true);
                JSONOutput.put("PCState", 3);
                startStop.setText("STOP");
                motorRuning = true;
            } else {
            /*Stop the line*/
                JSONOutput.put("TransmissionManualDebugging", false);
                JSONOutput.put("PCState", 1);
                startStop.setText("START");
                motorRuning = false;
            }
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }
}
