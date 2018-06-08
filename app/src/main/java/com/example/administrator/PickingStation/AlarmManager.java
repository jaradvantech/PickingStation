package com.example.administrator.PickingStation;


import android.content.Context;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlarmManager {

    private ArrayList<AlarmObject> newAlarms;
    private Context context;
    private int lastChecked_CommonAlarms;
    private int lastChecked_Manipulatoralarms[] = new int[5+1]; //todo MAGIC NUMBER_5

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
     * Will return an array o AlarmObject with the new alarmswwwwwww
     */
    public ArrayList<AlarmObject> parseAlarmCMD(String CMD) {
        newAlarms = new ArrayList<>();
        int current_manipulatoralarms[] = new int[5+1]; //todo MAGIC NUMBER_5
        int new_manipulatoralarms[] = new int[5+1]; //todo MAGIC NUMBER_5
        int new_commonAlarms = 0;

        //Ex. CMD=  CHAL_14_00000_00000_00000_00000_00002_00000
        //RBS TODO make this armnumber-independent
        int current_commonAlarms = Integer.parseInt(CMD.substring(8,12));
        current_manipulatoralarms[1] = Integer.parseInt(CMD.substring(14,19));
        current_manipulatoralarms[2] = Integer.parseInt(CMD.substring(20,25));
        current_manipulatoralarms[3] = Integer.parseInt(CMD.substring(26,31));
        current_manipulatoralarms[4] = Integer.parseInt(CMD.substring(32,37));
        current_manipulatoralarms[5] = Integer.parseInt(CMD.substring(38));

        if(current_commonAlarms != lastChecked_CommonAlarms) {
            /*Alarms have changed. check which bits are different (bitwise XOR)
             *  AND are now set (bits that changed from 1 to 0 are not relevant,
             *  We are only interested in newly fired alarms)
             */
            new_commonAlarms = (lastChecked_CommonAlarms^current_commonAlarms)&current_commonAlarms;
        }

        for(int i=1; i<=5; i++) { //todo MAGIC NUMBER_5
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
        for(int j=1; j<=5; j++) { //todo MAGIC NUMBER_5
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
        Boolean armState[] = new Boolean[6];

        if(lastChecked_CommonAlarms != 0) {
            armState[0] = true;
        }else{
            armState[0] = false;
        }
        for(int i=1; i<6; i++) {
            if(lastChecked_Manipulatoralarms[i] != 0) {
                armState[i] = true;
            }else{
                armState[i] = false;
            }
        }
        return armState;
    }

    Boolean charToBoolean(char c)
    {
        if(c=='0')
            return true;
        else
            return false;
    }
}
