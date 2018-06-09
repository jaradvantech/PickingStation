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

import static android.app.Activity.RESULT_OK;

public class Debug extends Fragment {

    private OnFragmentInteractionListener mFragmentInteraction;
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
                mFragmentInteraction.onSendCommand("PGSI_14_" + String.format("%02d", currentArm) + "\r\n"); //Starting at index
            }
        });

        buttonWrite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFragmentInteraction.onSendCommand(writeDebugData());
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
        armPosition.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AdvancedSettings.class);
                startActivityForResult(i, 1);
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
                mFragmentInteraction.onSendCommand("PGSI_14_" + String.format("%02d", currentArm));
            }
        });

        debug_previous.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (currentArm > 1) {
                    currentArm--;
                    currentArmText.setText(Integer.toString(currentArm));
                }
                mFragmentInteraction.onSendCommand("PGSI_14_" + String.format("%02d", currentArm));
            }
        });

        Runnable autoUpdater = new Runnable() {
            @Override
            public void run() {
                if (autoUpdate.isChecked()) {
                    mFragmentInteraction.onSendCommand("PGSI_14_" + String.format("%02d", currentArm));
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

    public void updateDebugData(String mCmd) {
        try {
            if ((commonDataOutput != null) && (armDataOutput != null)) {
                //Delete old data
                commonDataOutput.setText("");
                armDataOutput.setText("");

                //Write New data
                armDataOutput.append("### Data for arm number " + mCmd.substring(8, 10) + ":\n\n");
                armDataOutput.append(">HasDischarged; " + mCmd.charAt(11) + "\n");
                armDataOutput.append(">PhotosensorOfManipulator; " + mCmd.charAt(12) + "\n");
                armDataOutput.append(">ManipulatorStatePosition; " + mCmd.charAt(13) + "\n");
                armDataOutput.append(">DischargedTheBrickConfirm; " + mCmd.charAt(14) + "\n");
                armDataOutput.append(">LeftStorageBinSecurity; " + mCmd.charAt(15) + "\n");
                armDataOutput.append(">RightStorageBinSecurity; " + mCmd.charAt(16) + "\n");
                armDataOutput.append(">AlarmArray; " + mCmd.substring(18, 23) + "\n");
                armDataOutput.append(">ManipulatorRepositionState; " + mCmd.charAt(24) + "\n");
                armDataOutput.append(">ActualValueEncoder; " + mCmd.substring(26, 36) + "\n\n");

                commonDataOutput.append("### Common Data: \n\n");
                commonDataOutput.append(">TheQueueOfPhotosensor_1; " + mCmd.charAt(37) + "\n");
                commonDataOutput.append(">TheQueueOfPhotosensor_2; " + mCmd.charAt(38) + "\n");
                commonDataOutput.append(">TheQueueOfPhotosensor_3; " + mCmd.charAt(39) + "\n");
                commonDataOutput.append(">TheQueueOfPhotosensor_4; " + mCmd.charAt(40) + "\n");
                commonDataOutput.append(">StationInterlock_16; " + mCmd.charAt(41) + "\n");
                commonDataOutput.append(">WhetherOrNotPutTheTileTo_16; " + mCmd.charAt(42) + "\n");
                commonDataOutput.append(">EquipmentAlarmArray; " + mCmd.substring(44, 49) + "\n");
                commonDataOutput.append(">TileGrade; " + mCmd.substring(50, 53) + "\n");
                commonDataOutput.append(">ChangeColor; " + mCmd.substring(54, 57) + "\n");
                commonDataOutput.append(">SystemState; " + mCmd.substring(58, 61) + "\n");
                commonDataOutput.append(">ActualValueOfTheLineEncoder; " + mCmd.substring(62, 72) + "\n");
                commonDataOutput.append(">EnterTheTileStartingCodeValue; " + mCmd.substring(73, 83) + "\n");

                lastChecked_encoderValue = Integer.parseInt(mCmd.substring(62, 72));
            }

        } catch (Exception PGSIexc){
            Log.e("PGSI Exception", PGSIexc.toString());
        }
    }

    public String writeDebugData() {
        String writeCommand = "PWDA_14_";

        writeCommand += String.format("%02d", currentArm);
        writeCommand += "_";
        writeCommand += boolToString(checkBox_SBD.isChecked());
        writeCommand += boolToString(checkBox_MR.isChecked());
        writeCommand += boolToString(checkBox_SBFA.isChecked());
        writeCommand += boolToString(checkBox_SBFB.isChecked());
        writeCommand += boolToString(checkBox_BCRSA.isChecked());
        writeCommand += boolToString(checkBox_BCRSB.isChecked());
        writeCommand += boolToString(checkBox_MM.isChecked());
        writeCommand += boolToString(checkBox_VV.isChecked());
        writeCommand += "_";
        writeCommand += inputToString(editText_MFB, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_MLR, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_MUD, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_COD, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_WTDWT, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_PZA, "%06d");
        writeCommand += "_";
        writeCommand += inputToString(editText_VOCD, "%06d");
        writeCommand += "_";
        writeCommand += boolToString(checkBox_CE.isChecked());
        writeCommand += boolToString(checkBox_TP.isChecked());
        writeCommand += boolToString(checkBox_ITT.isChecked());
        writeCommand += boolToString(checkBox_TMD.isChecked());
        writeCommand += "_";
        writeCommand += inputToString(editText_PCS, "%03d");
        writeCommand += "_";
        writeCommand += inputToString(editText_ADD, "%06d");
        writeCommand += "_";
        writeCommand += inputToString(editText_ZASV, "%06d");
        writeCommand += "_";
        writeCommand += inputToString(editText_TPOX_AGB, "%06d");
        writeCommand += "_";
        writeCommand += inputToString(editText_TPOX_AAD, "%06d");
        writeCommand += "\r\n";

        return writeCommand;
    }


    String boolToString(Boolean mBol) {
        if (mBol)
            return "1";
        else
            return "0";
    }

    /*
     *RBS: Read number from input and convert to properly formatted String
     */
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

    //TODO this wont be needed

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                String sentCMD = data.getStringExtra("CMDstring");
                mFragmentInteraction.onSendCommand(sentCMD);

            }
        }
    }

}
