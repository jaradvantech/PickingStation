package com.example.administrator.PickingStation;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.util.ArrayList;

public class AppBarManager {

    public static final int CONNECTED = 1;
    public static final int DISCONNECTED= -1;
    private static ImageView appbar_connection;
    private static ImageView manipulatorAlarmIcons[];
    private static ImageView manipulatorAlarmModes[];
    private static ImageView equipmentAlarmIcon;
    private static LinearLayout appbarIcons;
    private static Activity activity;
    private static int currentArms;

    public static void initAppBarManager(final Activity mActivity) {
        activity = mActivity;

        appbar_connection = activity.findViewById(R.id.appbar_imageView_connection);

        appbarIcons = activity.findViewById(R.id.appBar_linearLayout_alarms);
        appbarIcons.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Activity fa = activity;
                ((MainActivity) activity).switchToLayout(R.id.nav_alarms);
            }
        });

        //drawIcons(SettingManager.getArms());
        drawIcons(7);
    }

    private static void drawIcons(int manipulators) {
        currentArms = manipulators;
        manipulatorAlarmIcons = new ImageView[currentArms];
        manipulatorAlarmModes = new ImageView[currentArms];
        appbarIcons.removeAllViews();

        //Manipulator alarms
        for(int i=(currentArms-1); i>=0; i--) {
            View iconToAdd = activity.getLayoutInflater().inflate(R.layout.appbar_icon, appbarIcons, false);
            manipulatorAlarmIcons[i] = iconToAdd.findViewById(R.id.icon_imageView_icon);
            manipulatorAlarmModes[i] = iconToAdd.findViewById(R.id.icon_imageView_mode);
            manipulatorAlarmModes[i].setVisibility(View.INVISIBLE);
            appbarIcons.addView(iconToAdd);
        }

        //Equipment alarm
        equipmentAlarmIcon = new ImageView(activity);
        equipmentAlarmIcon.setImageResource(R.mipmap.equipment);
        LinearLayout.LayoutParams equipmentIconParams = new LinearLayout.LayoutParams(32, 32);
        equipmentIconParams.gravity = Gravity.CENTER;
        equipmentIconParams.setMargins(0,0,8,0);
        equipmentAlarmIcon.setLayoutParams(equipmentIconParams);
        appbarIcons.addView(equipmentAlarmIcon);
    }


    public static void updateEquipmentAlarm(boolean equipmentAlarm) {
        if(equipmentAlarm) {
            equipmentAlarmIcon.setColorFilter(Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            equipmentAlarmIcon.clearColorFilter();
        }
    }

    public static void updateManipulatorAlarms(boolean[] manipulatorAlarms) {
        int receivedArmNumber = manipulatorAlarms.length;
        if(manipulatorAlarms.length != currentArms) {
            Log.e("AppBar Manager", "Number of manipualtors has changed from " + currentArms + " to " + receivedArmNumber + "!!!!" );
            SettingManager.setArms(receivedArmNumber);
            drawIcons(receivedArmNumber);
        }

        for(int i=0; i<currentArms; i++) {
            if(manipulatorAlarms[i]) {
                manipulatorAlarmIcons[i].setColorFilter(Color.rgb(255, 0, 0), android.graphics.PorterDuff.Mode.MULTIPLY);
            } else {
                manipulatorAlarmIcons[i].clearColorFilter();
            }
        }
    }

    public static void updateAppbarModes(boolean[] modes) {
        //0 = auto, 1 = manual
        for(int i = 0; i<SettingManager.getArms(); i++) {
            if(modes[i]) {
                manipulatorAlarmModes[i].setVisibility(View.VISIBLE);
            } else {
                manipulatorAlarmModes[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    public static void updateConnectionIcon(int status) {
        if (status == CONNECTED) {
            appbar_connection.setImageResource(R.mipmap.linkup);
            appbar_connection.clearColorFilter();

        } else if (status == DISCONNECTED) {
            appbar_connection.setImageResource(R.mipmap.linkdown);
            appbar_connection.setColorFilter(Color.rgb(115, 0, 0));
        }
    }

}
