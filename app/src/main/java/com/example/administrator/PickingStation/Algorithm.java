package com.example.administrator.PickingStation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.administrator.PickingStation.Commands.ALGC;


public class Algorithm extends Fragment {
    private AlertDialog.Builder dialogBuilder;
    private boolean unsavedChanges = false;
    private OnFragmentInteractionListener mFragmentInteraction;
    private int manipulatorModes[];
    private ImageView imageViews[];
    private TextView textViews[];
    private TextView demoBrick;
    private ListView algorithm_listView_colours;
    private ListView algorithm_listView_grades;
    private int currentColor = 1;
    private int currentGrade = 1;

    private LayoutInflater inflater;
    private ViewGroup container;
    private LinearLayout holder;
    private View view;
    private int armNumber;

    private final int INPUT = 0;
    private final int INOUT = 1;
    private final int OUTPUT = 2;
    private final int DISABLED = 3;

    public Algorithm() {
    }

    public void onCreate(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_algorithm, container, false);
        this.holder = view.findViewById(R.id.algorithm_linearLayout_modes);
        this.demoBrick = (TextView) view.findViewById(R.id.algorithm_demobrick);
        Button saveButton = (Button) view.findViewById(R.id.algorithm_button_save);
        this.inflater = inflater;
        this.container = container;

        //setManipulatorNumber(SettingManager.getArms());
        setManipulatorNumber(7);


        //SAVE DIALOG
        dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(getString(R.string.Warning));
        dialogBuilder.setMessage(getString(R.string.Saveconfigurationtomachine));
        //menu buttons listener
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        saveAlgorithmConfiguration();
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        dialogBuilder.setPositiveButton(getString(R.string.Save), dialogClickListener);
        dialogBuilder.setNeutralButton(getString(R.string.Cancel), dialogClickListener);

        //Default: hide demo brick until configuration is received from server
        demoBrick.setVisibility(View.INVISIBLE);

        //SAVE BUTTON
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog dialog = dialogBuilder.create();
                dialog.setIcon(R.mipmap.faq);
                BiggerDialogs.show(dialog);
            }
        });

        /*Listview*************************************************************/
        algorithm_listView_colours=(ListView) view.findViewById(R.id.algorithm_listView_colours);
        algorithm_listView_grades=(ListView) view.findViewById(R.id.algorithm_listView_grades);
        algorithm_listView_colours.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        algorithm_listView_grades.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        algorithm_listView_colours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                currentColor = position;
                updateDisplay();
                unsavedChanges = true;
            }
        });

        algorithm_listView_grades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                currentGrade = position;
                updateDisplay();
                unsavedChanges = true;
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

    public void setManipulatorNumber(int number) {
        this.armNumber = number;
        manipulatorModes = new int[armNumber];
        View currentViews[] = new View[armNumber]; //RBS maybe this is not needed.
        imageViews = new ImageView[armNumber];
        textViews = new TextView[armNumber];
        holder.removeAllViews();

        for(int i=0; i<armNumber; i++) {
            currentViews[i] = inflater.inflate(R.layout.algorithm_segment, container, false);
            imageViews[i] = currentViews[i].findViewById(R.id.algorithmSegment_imageView_mode);
            textViews[i] = currentViews[i].findViewById(R.id.algorithmSegment_textView_text);
            holder.addView(currentViews[i]);
        }

        //Create listeners
        for(int i=0; i<armNumber; i++) {
            final int fi = i;
            imageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Algorithm.this.updateModes(fi);
                    unsavedChanges = true;
                }
            });
        }
        updateDisplay();
    }

    private void updateModes(int mManipulator) {
        manipulatorModes[mManipulator] += 1;
        if (manipulatorModes[mManipulator] > 3)
            manipulatorModes[mManipulator] = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        for (int i = 0; i<armNumber; i++) {
            if (manipulatorModes[i] == INPUT) {
                imageViews[i].setImageResource(R.mipmap.in);
                textViews[i].setText(getString(R.string.Arm) + " " + (i+1) +getString(R.string.INPUT));

            } else if (manipulatorModes[i] == INOUT) {
                imageViews[i].setImageResource(R.mipmap.inout);
                textViews[i].setText(getString(R.string.Arm) + " " + (i+1) +getString(R.string.INOUT));

            } else if (manipulatorModes[i] == OUTPUT) {
                imageViews[i].setImageResource(R.mipmap.out);
                textViews[i].setText(getString(R.string.Arm) + " " + (i+1) +getString(R.string.OUTPUT));

            } else if(manipulatorModes[i] == DISABLED){
                imageViews[i].setImageResource(R.mipmap.disabled);
                textViews[i].setText(getString(R.string.Arm) + " " + (i+1) +getString(R.string.DISABLED));
            }
        }
        setDemoBrickColor(currentColor);
        setDemoBrickGrade(currentGrade);
    }

    public void retrieveAlgorithmConfiguration() {
        mFragmentInteraction.onSendCommand(ALGC);
    }

    public void onAlgorithmConfigurationRetrieved(String CMD) {
        try {
            JSONObject JSONparser = new JSONObject(CMD);
            currentGrade = JSONparser.getInt("currentGrade");
            currentColor = JSONparser.getInt("currentColor");
            demoBrick.setVisibility(View.VISIBLE);

            JSONArray arrayMode = JSONparser.getJSONArray("manipulatorModes");

            int receivedArmNumber = arrayMode.length();
            if(receivedArmNumber != armNumber) {
                Log.e("Algorithm", "Number of arms has changed from " + armNumber + " to " + receivedArmNumber + "!!!!" );
                SettingManager.setArms(receivedArmNumber);
                this.setManipulatorNumber(receivedArmNumber);
            }

            for(int i=0; i<arrayMode.length(); i++) {
                manipulatorModes[i] = arrayMode.getInt(i);
            }
            updateDisplay();

        } catch (Exception jsonExc) {
            Log.e("JSON Exception", "onAlgorithmConfigurationRetrieved(): " + jsonExc.getMessage());
        }
    }

    public void saveAlgorithmConfiguration(){
        unsavedChanges = false;

        try {
            JSONObject JSONOutput = new JSONObject();

            JSONOutput.put("command_ID", "ALSC");
            JSONOutput.put("color",  algorithm_listView_colours.getCheckedItemPosition());
            JSONOutput.put("grade", algorithm_listView_grades.getCheckedItemPosition());

            JSONArray modes = new JSONArray();
            for(int i=0; i<armNumber; i++)
                modes.put(manipulatorModes[i]);
            JSONOutput.put("modes", modes);

            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");

        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    public void setDemoBrickGrade(int mGrade) {
        demoBrick.setText(BrickManager.getGrade(mGrade));
    }

    public void setDemoBrickColor(int mColor) {
        demoBrick.setBackgroundColor(mColor);
    }

    public void whenEnteringFragment() {
        retrieveAlgorithmConfiguration();
    }

    public void whenLeavingFragment() {
        if(unsavedChanges) {
            AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(getActivity());
            saveDialogBuilder.setTitle(getString(R.string.Warning));
            saveDialogBuilder.setMessage("Save changes made to the Algorithm Configuration?"); //TODO RBS STRINGS
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            saveAlgorithmConfiguration();
                            break;

                        case DialogInterface.BUTTON_NEUTRAL:
                            break;
                    }
                }
            };
            saveDialogBuilder.setPositiveButton("Continue", dialogClickListener); //TODO RBS STRINGS
            saveDialogBuilder.setNeutralButton(getString(R.string.Cancel), dialogClickListener);
            AlertDialog dialog = saveDialogBuilder.create();
            dialog.setIcon(R.mipmap.warning);
            BiggerDialogs.show(dialog);
        }
        unsavedChanges = false;
    }

    public void onLostConnection() {
        demoBrick.setVisibility(View.INVISIBLE);
        for(int i=0; i<imageViews.length; i++) {
            imageViews[i].setVisibility(View.INVISIBLE);
            textViews[i].setVisibility(View.INVISIBLE);
        }
    }

    public void onEstablishedConnection() {
        demoBrick.setVisibility(View.VISIBLE);
        for(int i=0; i<imageViews.length; i++) {
            imageViews[i].setVisibility(View.VISIBLE);
            textViews[i].setVisibility(View.VISIBLE);
        }

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                //Run 500ms after in case the server is just started
                //and not ready yet for answering commands
                //RBS TODO yes, not the most elegant solution...
                retrieveAlgorithmConfiguration();
            }
        }, 500);
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }
}
