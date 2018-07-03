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
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.example.administrator.PickingStation.Commands.ALGC;


public class Algorithm extends Fragment {
    private AlertDialog.Builder dialogBuilder;
    private final int MANIPULATORS = 5;
    private boolean unsavedChanges = false;
    private OnFragmentInteractionListener mFragmentInteraction;
    private int manipulatorModes[]  = new int[MANIPULATORS];
    private ImageView imageViews[] = new ImageView[MANIPULATORS];
    private TextView textViews[] = new TextView[MANIPULATORS];
    private TextView demoBrick;
    private ListView algorithm_listView_colours;
    private ListView algorithm_listView_grades;
    private int currentColor = 1;
    private int currentGrade = 1;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //TODO manipulator number independent
        View view = inflater.inflate(R.layout.fragment_algorithm, container, false);
        imageViews[0] = (ImageView) view.findViewById(R.id.algorithm_imageView_arm1mode);
        imageViews[1] = (ImageView) view.findViewById(R.id.algorithm_imageView_arm2mode);
        imageViews[2] = (ImageView) view.findViewById(R.id.algorithm_imageView_arm3mode);
        imageViews[3] = (ImageView) view.findViewById(R.id.algorithm_imageView_arm4mode);
        imageViews[4] = (ImageView) view.findViewById(R.id.algorithm_imageView_arm5mode);
        textViews[0] = (TextView) view.findViewById(R.id.algorithm_textView_manipulator1);
        textViews[1] = (TextView) view.findViewById(R.id.algorithm_textView_manipulator2);
        textViews[2] = (TextView) view.findViewById(R.id.algorithm_textView_manipulator3);
        textViews[3] = (TextView) view.findViewById(R.id.algorithm_textView_manipulator4);
        textViews[4] = (TextView) view.findViewById(R.id.algorithm_textView_manipulator5);
        demoBrick = (TextView) view.findViewById(R.id.algorithm_demobrick);
        Button saveButton = (Button) view.findViewById(R.id.algorithm_button_save);

        updateDisplay();

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

        imageViews[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Algorithm.this.updateModes(0);
                unsavedChanges = true;
            }
        });
        imageViews[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Algorithm.this.updateModes(1);
                unsavedChanges = true;
            }
        });
        imageViews[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Algorithm.this.updateModes(2);
                unsavedChanges = true;
            }
        });
        imageViews[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Algorithm.this.updateModes(3);
                unsavedChanges = true;
            }
        });
        imageViews[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Algorithm.this.updateModes(4);
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

    private void updateModes(int mManipulator) {
        manipulatorModes[mManipulator] += 1;
        if (manipulatorModes[mManipulator] > 3)
            manipulatorModes[mManipulator] = 0;
        updateDisplay();
    }

    private void updateDisplay() {
        for (int i = 0; i<MANIPULATORS; i++) {
            if (manipulatorModes[i] == INPUT) {
                imageViews[i].setImageResource(R.mipmap.in);
                textViews[i].setText(getString(R.string.Arm) + " " + i +getString(R.string.INPUT));

            } else if (manipulatorModes[i] == INOUT) {
                imageViews[i].setImageResource(R.mipmap.inout);
                textViews[i].setText(getString(R.string.Arm) + " " + i +getString(R.string.INOUT));

            } else if (manipulatorModes[i] == OUTPUT) {
                imageViews[i].setImageResource(R.mipmap.out);
                textViews[i].setText(getString(R.string.Arm) + " " + i +getString(R.string.OUTPUT));

            } else if(manipulatorModes[i] == DISABLED){
                imageViews[i].setImageResource(R.mipmap.disabled);
                textViews[i].setText(getString(R.string.Arm) + " " + i +getString(R.string.DISABLED));
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
            for(int i=0; i<MANIPULATORS; i++)
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

    public void setDemoBrickColor(int mColor ) {
        demoBrick.setBackgroundColor(BrickManager.getColor(mColor));
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
