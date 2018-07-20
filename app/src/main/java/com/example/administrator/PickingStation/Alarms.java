package com.example.administrator.PickingStation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.administrator.PickingStation.Commands.CHAL;


public class Alarms extends Fragment {

    private Alarms.OnFragmentInteractionListener mFragmentInteraction;
    private AlertDialog.Builder builder;
    private AlarmListAdapter adapter;
    private int lastSelectedItem = 0;
    private TextView infoOutput;
    private ArrayList<AlarmObject> theAlarms;
    private ListView mListView;
    private final int ALARM_CHECK_PERIOD = 2000;
    private final Handler alarmLoopHandler = new Handler(Looper.getMainLooper());
    private int totalManipulators;
    private ArrayList<AlarmObject> newAlarms;
    private int lastChecked_CommonAlarms;
    private int lastChecked_Manipulatoralarms[];

    public Alarms() {
    }

    public static Alarms newInstance(String param1, String param2) {
        Alarms fragment = new Alarms();
        Bundle args = new Bundle();

        return fragment;
    }


    public void onCreate(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof Alarms.OnFragmentInteractionListener) {
            mFragmentInteraction = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarms, container, false);

        setManipulatorNumber(SettingManager.getArms());

        TextView textView_recent = (TextView) view.findViewById(R.id.alarms_textView_recent);
        infoOutput = (TextView) view.findViewById(R.id.alarms_textView_output);
        infoOutput.setMovementMethod(new ScrollingMovementMethod());

        mListView = (ListView) view.findViewById(R.id.alarms_ListView);
        theAlarms = new ArrayList<>();

        //Set adapter
        adapter = new AlarmListAdapter(getActivity(), theAlarms);
        mListView.setAdapter(adapter);


        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage(getString(R.string.Doyoureallywanttodeletethisalarm));
        //menu buttons listener
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                            theAlarms.remove(lastSelectedItem);
                            adapter.notifyDataSetChanged();
                     break;

                    case DialogInterface.BUTTON_NEGATIVE:
                    break;
                }
            }
        };
        builder.setPositiveButton(getString(R.string.Delete), dialogClickListener);
        builder.setNegativeButton(getString(R.string.Cancel), dialogClickListener);

        //User interaction: Delete Alarm
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedItem = position;
                AlertDialog dialog = builder.create();
                dialog.setIcon(R.mipmap.warning);
                BiggerDialogs.show(dialog);
                return true;
             }
        });


        ImageView clearButton = view.findViewById(R.id.alarms_imageView_clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theAlarms.clear();
                adapter.notifyDataSetChanged();
                infoOutput.setText("");
            }
        });

        //Periodically check for alarms
        Runnable autoUpdater = new Runnable() {
            @Override
            public void run() {
                mFragmentInteraction.onSendCommand(CHAL);
                alarmLoopHandler.postDelayed(this, ALARM_CHECK_PERIOD);
            }
        };
        autoUpdater.run();
        return view;
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setManipulatorNumber(int number) {
        lastChecked_Manipulatoralarms = new int[number];
        totalManipulators = number;
    }

    public void updateAlarmLog(ArrayList<AlarmObject> newAlarms){
        //Append new alarms to alarm list
        theAlarms.addAll(newAlarms);

        //Sort alarms by decreasing timestamp, so new ones are on top
        //theAlarms.sort(new AlarmObject.SortByDate);

        //Update listview
        adapter.notifyDataSetChanged();
    }

    /* RBS
     *Parse alarm command:
     * Will check new alarm codes against previosly received alarm commands
     * in order to detect only the newly generated alarms.
     * Will return an array o AlarmObject with the new alarms
     */
    public ArrayList<AlarmObject> parseAlarmCMD(String CMD) {
        newAlarms = new ArrayList<>();
        int current_manipulatoralarms[] = new int[totalManipulators+1]; //TODO RBS this other index salad has to be fixed as well
        int new_manipulatoralarms[] = new int[totalManipulators+1];
        int new_commonAlarms=0, current_commonAlarms=0;

        try {
            JSONObject JSONparser = new JSONObject(CMD);
            JSONArray manipulatorAlarmArray = JSONparser.getJSONArray("manipulatorAlarms");

            int receivedManipulatorNumber = manipulatorAlarmArray.length();
            if(receivedManipulatorNumber != totalManipulators) {
                Log.e("Alarms", "Number of manipualtors has changed from " + totalManipulators + " to " + receivedManipulatorNumber + "!!!!" );
                SettingManager.setArms(receivedManipulatorNumber);
                setManipulatorNumber(receivedManipulatorNumber);
            }

            for(int i=0; i<manipulatorAlarmArray.length(); i++) {
                current_manipulatoralarms[i+1] = manipulatorAlarmArray.getInt(i);
            }
            current_commonAlarms = JSONparser.getInt("equipmentAlarms");
        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }

        if(current_commonAlarms != lastChecked_CommonAlarms) {
            /*Alarms have changed. check which bits are different (bitwise XOR)
             *  AND are now set (bits that changed from 1 to 0 are not relevant,
             *  We are only interested in newly fired alarms)
             */
            new_commonAlarms = (lastChecked_CommonAlarms^current_commonAlarms)&current_commonAlarms;
        }

        for(int i=0; i<totalManipulators; i++) {
            if(current_manipulatoralarms[i] != lastChecked_Manipulatoralarms[i]){
                new_manipulatoralarms[i] = (lastChecked_Manipulatoralarms[i]^current_manipulatoralarms[i])&current_manipulatoralarms[i];
            }
        }

        //Update values.
        lastChecked_Manipulatoralarms = current_manipulatoralarms;
        lastChecked_CommonAlarms = current_commonAlarms;

        /*Create Alarm Objects*******************************/
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm:ss dd-MM-yy ");
        String strDate = mdformat.format(calendar.getTime());

        //Equipment
        for(int i=0; i<16; i++) {
            //check bit at i (16 bits in use for alarms)
            if(BigInteger.valueOf(new_commonAlarms).testBit(i)){
                //If bit is set: we detected a new alarm.
                //Type=i+13 because (1-12) are manipulator alarms and (13-20) are equipment alarms
                String alarmDescription = getContext().getResources().getString(
                        getContext().getResources().getIdentifier("alarm_" + (i+13), "string", getContext().getPackageName()));
                newAlarms.add(new AlarmObject(Integer.toString(i+13), strDate, getContext().getResources().getString(R.string.Equipment), alarmDescription));
            }
        }
        //Arm Alarms
        for(int j=0; j<totalManipulators; j++) {
            //check bit at i
            for(int i=0; i<16; i++) {
                //check bit at i
                if(BigInteger.valueOf(new_manipulatoralarms[j]).testBit(i)){
                    String alarmDescription = getContext().getResources().getString(
                            getContext().getResources().getIdentifier("alarm_" + (i+1), "string", getContext().getPackageName()));
                    newAlarms.add(new AlarmObject(Integer.toString(i+1), strDate.toString(), getContext().getResources().getString(R.string.Manipulator) + " "  + (j+1), alarmDescription));
                }
            }
        }

        updateCurrentAlarms();
        return newAlarms;
    }

    private void updateCurrentAlarms() {

        //Update appbar
        boolean[] alarms = new boolean[totalManipulators];
        for(int i=0; i<totalManipulators; i++) {
            if(lastChecked_Manipulatoralarms[i] != 0) {
                alarms[i] = true;
            }else{
                alarms[i] = false;
            }
        }

        AppBarManager.updateEquipmentAlarm(lastChecked_CommonAlarms != 0);
        AppBarManager.updateManipulatorAlarms(alarms);

        //update text field
        infoOutput.setText("");
        infoOutput.append("Equipment:\n");
        if(lastChecked_CommonAlarms != 0) {
            for(int i=0; i<16; i++) {
                if(BigInteger.valueOf(lastChecked_CommonAlarms).testBit(i)){
                    String alarmDescription = getContext().getResources().getString(
                            getContext().getResources().getIdentifier("alarm_" + (i+13), "string", getContext().getPackageName()));
                    infoOutput.append("    Bit "+i+": "+alarmDescription+"\n");
                }
            }
        } else {
            infoOutput.append("    No alarms ✔\n");
        }
        infoOutput.append("\n");

        for(int j=0; j<totalManipulators; j++) {
            infoOutput.append("Manipulator "+(j+1)+":\n");
            if(lastChecked_Manipulatoralarms[j] != 0) {
                for (int i = 0; i < 16; i++) {
                    if (BigInteger.valueOf(lastChecked_Manipulatoralarms[j]).testBit(i)) {
                        String alarmDescription = getContext().getResources().getString(
                                getContext().getResources().getIdentifier("alarm_" + (i + 1), "string", getContext().getPackageName()));
                        infoOutput.append("    Bit " + i + ": " + alarmDescription + "\n");
                    }
                }
            } else {
                infoOutput.append("    No alarms ✔\n");
            }
            infoOutput.append("\n");
        }

    }


    public void whenEnteringFragment() {

    }

    public void whenLeavingFragment() {

    }

    public interface OnFragmentInteractionListener {
        void onSendCommand(String command);
    }
}

/* ALARM INFORMATION
From PLC program doc

(type 1, bit 1)     DB2.DBX34.0	    X轴撞击限位报警	        X shaft impact limit alarm
(type 2, bit 2)     DB2.DBX34.1 	Z轴撞击限位报警	         Z shaft impact limit alarm
(type 3, bit 3)     DB2.DBX34.2 	W轴撞击限位报警	        W shaft impact limit alarm
(type 4, bit 4)     DB2.DBX34.3	    吸盘限位开关报警	        Suction cup limit switch alarm
(type 5, bit 5)     DB2.DBX34.4	    1#吸盘压力开关报警	    1# suction disc pressure switch alarm
(type 6, bit 6)     DB2.DBX34.5 	2#吸盘压力开关报警	    2# suction disc pressure switch alarm
(type 7, bit 7)     DB2.DBX34.6 	3#吸盘压力开关报警	    3# suction disc pressure switch alarm
(type 8, bit 8)     DB2.DBX34.7 	4#吸盘压力开关报警	    4# suction disc pressure switch alarm
(type 9, bit 9)     DB2.DBX35.0 	1#库位安全光电报警	    1# library security optoelectronic alarm
(type 10, bit 10) DB2.DBX35.1 	2#库位安全光电报警	    2# library security optoelectronic alarm
(type 11, bit 11) DB2.DBX35.2 	X轴变频器报警	            X axis frequency converter alarm
(type 12, bit 12) DB2.DBX35.3 	Z轴变频器报警	            Z axis frequency converter alarm
                         DB2.DBX35.4 	not used
                         DB2.DBX35.5 	not used
                         DB2.DBX35.6 	not used
                         DB2.DBX35.7 	not used


(type 13, bit  1)   DB2.DBX2.0	主控柜急停               	Main control cabinet emergency stop
(type 14, bit  1)   DB2.DBX2.1	输送线入口左急停	        Transmission line entrance left emergency stop
(type 15, bit  1)   DB2.DBX2.2	输送线出口急停         	Exportation of transmission line
(type 16, bit  1)   DB2.DBX2.3	输送线变频器报警	        Transmission line frequency converter alarm
(type 17, bit  1)   DB2.DBX2.4	主气源压力不足	            Insufficient pressure of main gas source
(type 18, bit  1)   DB2.DBX2.5	输送线入口右急停	        Transmission line entrance right stop
(type 19, bit  1)   DB2.DBX2.6	与06PLC断开连接	        Disconnect with 06PLC
(type 20, bit  1)   DB2.DBX2.7	与18PLC断开连接	        Disconnect with 18PLC
                         DB2.DBX3.0	not used
                         DB2.DBX3.1	not used
                         DB2.DBX3.2	not used
                         DB2.DBX3.3	not used
                         DB2.DBX3.4	not used
                         DB2.DBX3.5	not used
                         DB2.DBX3.6	not used
                         DB2.DBX3.7	not used

 */