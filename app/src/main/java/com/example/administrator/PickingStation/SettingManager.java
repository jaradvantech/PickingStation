package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingManager {

    /*
     * Each Manipulator needs two channels, and every server has 4.
     * Default is 3
     */
    private static int numberOfRFIDServers = 3;
    private static Context appContext;
    private static Gson gson;

    static void initSettingManager(Context mContext) {
        appContext = mContext;
        gson = new Gson();
    }

    static ArrayList<SettingObject> getFromPreferences() {
        SharedPreferences sharedPref = appContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        String jsonPreferences = sharedPref.getString("USER_SETTINGS", "");
        Type type = new TypeToken<List<SettingObject>>() {}.getType();

        ArrayList<SettingObject> settings = gson.fromJson(jsonPreferences, type);
        if(settings == null) settings = new ArrayList<SettingObject>();

        return settings;
    }
    
    static String getSetting(String name) {
        String retVal = "";
        ArrayList<SettingObject> settings = getFromPreferences();
        for(int i=0; i<settings.size(); i++) {
            if(settings.get(i).setting.equals(name)) {
                retVal = settings.get(i).value;
            }
        }
        return retVal;
    }

    static void saveToPreferences(ArrayList<SettingObject> currentSettings) {
        SharedPreferences sharedPref = appContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //Serialize objects to a String that can be saved by using the Google Gson library.
        String serializedObjects = gson.toJson(currentSettings);
        editor.putString("USER_SETTINGS", serializedObjects);
        editor.commit();
    }

    static void loadDefaults() {
        ArrayList settingArrayList = new ArrayList<SettingObject>();
        settingArrayList.add(new SettingObject("Machine controller", "", "ip"));
        settingArrayList.add(new SettingObject("PLC Address", "", "ip"));
        for(int i=0; i<numberOfRFIDServers; i++)
            settingArrayList.add(new SettingObject("RFID server " + (i+1), "", "ip"));

        SharedPreferences sharedPref = appContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String serializedObjects = gson.toJson(settingArrayList);
        editor.putString("USER_SETTINGS", serializedObjects);
        editor.commit();
    }

    static boolean previousConfigurationExists() {
        SharedPreferences sharedPref = appContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        String settingsString = sharedPref.getString("USER_SETTINGS", "");
        return !settingsString.equals("");
    }

    /*
     *  Local IN-APP configurations.
     *  These are independent from the settings stored in the machine.
     */
    static int getLanguage() {
        SharedPreferences sharedPref = appContext.getSharedPreferences(appContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getInt("LANGUAGE", 0); //Default language is 0
    }

    static void setLanguage(int selectedLanguage) {
        SharedPreferences sharedPref = appContext.getSharedPreferences(appContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPref_editor = sharedPref.edit();
        sharedPref_editor.putInt("LANGUAGE", selectedLanguage);
        sharedPref_editor.commit();
    }

    static int getTotalManipulators() {
        SharedPreferences sharedPref = appContext.getSharedPreferences(appContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getInt("MANIPULATORS", 5); //Default Number is 5
    }

    static void setTotalManipulators(int numberOfManipulators) {
        SharedPreferences sharedPref = appContext.getSharedPreferences(appContext.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPref_editor = sharedPref.edit();
        sharedPref_editor.putInt("MANIPULATORS", numberOfManipulators);
        sharedPref_editor.commit();
        numberOfRFIDServers = (int) Math.ceil(numberOfManipulators / 2);
    }
}
