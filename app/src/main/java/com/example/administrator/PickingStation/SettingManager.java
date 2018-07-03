package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {

    private static Context appContext;

    static void initSettingManager(Context mContext) {
        appContext = mContext;
    }

    static IPsetting getMachineControllerAddress() {
        SharedPreferences sharedPref = appContext.getSharedPreferences("IPADDRESS", Context.MODE_PRIVATE);
        return new IPsetting("MachineController",  sharedPref.getString("IPADDRESS", "000.000.000.000"), sharedPref.getString("PORT", "23623"));
    }

    static void setMachineControllerAddress(IPsetting machineIP) {
        SharedPreferences sharedPref = appContext.getSharedPreferences("IPADDRESS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("IPADDRESS", machineIP.address);
        editor.commit();
        sharedPref = appContext.getSharedPreferences("PORT", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putString("PORT", machineIP.port);
        editor.commit();
    }

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
        //
    }
}
