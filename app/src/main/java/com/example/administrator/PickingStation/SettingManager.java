package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingManager {

    static ArrayList<SettingObject> getFromPreferences(Context mainApplicationContext) {
        Gson gson = new Gson();
        SharedPreferences sharedPref = mainApplicationContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        String jsonPreferences = sharedPref.getString("USER_SETTINGS", "");
        Type type = new TypeToken<List<SettingObject>>() {}.getType();

        ArrayList<SettingObject> settings = gson.fromJson(jsonPreferences, type);
        if(settings == null) settings = new ArrayList<SettingObject>();


        return settings;
    }
    
    static String getSetting(String name, Context mainApplicationContext) {
        String retVal = "";
        ArrayList<SettingObject> settings = getFromPreferences(mainApplicationContext);
        for(int i=0; i<settings.size(); i++) {
            if(settings.get(i).setting.equals(name)) {
                retVal = settings.get(i).value;
            }
        }
        return retVal;
    }
    
    static void saveSetting(SettingObject boxToSave, Context mainApplicationContext) {
        ArrayList<SettingObject> settings = getFromPreferences(mainApplicationContext);

        //Append design to array
        settings.add(boxToSave);

        //Save Array
        saveToPreferences(settings, mainApplicationContext);
    }

    static void saveToPreferences(ArrayList<SettingObject> currentSettings, Context mainApplicationContext) {
        Gson gson = new Gson();
        SharedPreferences sharedPref = mainApplicationContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        //Serialize objects to a String that can be saved by using the Google Gson library.
        String serializedObjects = gson.toJson(currentSettings);
        editor.putString("USER_SETTINGS", serializedObjects);
        editor.commit();
    }

    static void loadDefaults(Context mainApplicationContext) {
        ArrayList settingArrayList = new ArrayList<SettingObject>();
        settingArrayList.add(new SettingObject("Machine controller", "", "ip"));
        settingArrayList.add(new SettingObject("PLC Address", "", "ip"));
        settingArrayList.add(new SettingObject("RFID server 1", "", "ip"));
        settingArrayList.add(new SettingObject("RFID server 2", "", "ip"));
        settingArrayList.add(new SettingObject("RFID server 3", "", "ip"));

        Gson gson = new Gson();
        SharedPreferences sharedPref = mainApplicationContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String serializedObjects = gson.toJson(settingArrayList);
        editor.putString("USER_SETTINGS", serializedObjects);
        editor.commit();
    }

    static boolean previousConfigurationExists(Context mainApplicationContext) {
        SharedPreferences sharedPref = mainApplicationContext.getSharedPreferences("USER_SETTINGS", Context.MODE_PRIVATE);
        String settingsString = sharedPref.getString("USER_SETTINGS", "");
        return !settingsString.equals("");
    }
}
