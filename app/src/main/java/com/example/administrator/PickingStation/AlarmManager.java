package com.example.administrator.PickingStation;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmManager {

    private int MANIPULATORS = 5; //RBS, yes TODO
    private ArrayList<AlarmObject> newAlarms;
    private Context context;
    private int lastChecked_CommonAlarms;
    private int lastChecked_Manipulatoralarms[] = new int[MANIPULATORS+1];

    /*
     * Conyext is needed.
     */
    AlarmManager(Context mContext) {
        this.context = mContext;
    }

    /* RBS
     *Parse alarm command:
     * Will check new alarm codes against previosly received alarm commands
     * in order to detect only the newly generated alarms.
     * Will return an array o AlarmObject with the new alarms
     */
    public ArrayList<AlarmObject> parseAlarmCMD(String CMD) {
        newAlarms = new ArrayList<>();
        int current_manipulatoralarms[] = new int[MANIPULATORS+1];
        int new_manipulatoralarms[] = new int[MANIPULATORS+1];
        int new_commonAlarms=0, current_commonAlarms=0;

        try {
            JSONObject JSONparser = new JSONObject(CMD);
            JSONArray manipulatorAlarmArray = JSONparser.getJSONArray("manipulatorAlarms");
            for(int i=0; i<manipulatorAlarmArray.length(); i++) {
                current_manipulatoralarms[i] = manipulatorAlarmArray.getJSONObject(i).getInt("alarmArray");
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

        for(int i=1; i<=MANIPULATORS; i++) {
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
                //Type= position of bit in array. As we dont know the meaning yet
                newAlarms.add(new AlarmObject(Integer.toString(i), strDate, context.getResources().getString(R.string.Equipment)));
            }
        }
        //Arm Alarms
        for(int j=1; j<=MANIPULATORS; j++) {
            //check bit at i
            for(int i=0; i<16; i++) {
                //check bit at i
                if(BigInteger.valueOf(new_manipulatoralarms[j]).testBit(i)){
                    newAlarms.add(new AlarmObject(Integer.toString(i), strDate.toString(), context.getResources().getString(R.string.Manipulator) + " "  + j));
                }
            }
        }

        return newAlarms;
    }

    /*
     * Returns array of bools denoting which arms have alarms r n
     * Array starts @index 0
     *  True: alarms
     *  False: no alarms
     */
    public Boolean[] getCurrentArmState(){
        Boolean armState[] = new Boolean[MANIPULATORS+1];

        if(lastChecked_CommonAlarms != 0) {
            armState[0] = true;
        }else{
            armState[0] = false;
        }
        for(int i=1; i<(MANIPULATORS+1); i++) {
            if(lastChecked_Manipulatoralarms[i] != 0) {
                armState[i] = true;
            }else{
                armState[i] = false;
            }
        }
        return armState;
    }
}
