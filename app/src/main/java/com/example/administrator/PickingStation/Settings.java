package com.example.administrator.PickingStation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

import static com.example.administrator.PickingStation.SettingManager.getLanguage;


import static com.example.administrator.PickingStation.SettingManager.getMachineControllerAddress;
import static com.example.administrator.PickingStation.SettingManager.setLanguage;
import static com.example.administrator.PickingStation.SettingManager.setMachineControllerAddress;


public class Settings extends Fragment {

    private OnFragmentInteractionListener mFragmentInteraction;
    private int lastSelectedItem = 0;
    private FlippableStackView mFlippableStack;
    private ColorFragmentAdapter fragmentAdapter;
    private List<Fragment> fragmentList;
    private ArrayList<IPsetting> currentSettings;
    private ListView settingsListView;
    private SettingsListAdapter listAdapter;
    private View finalView;
    private ImageView save;
    private ImageView calibration;
    private ImageView factoryReset;
    private ImageView tileConfig;
    private AlertDialog.Builder resetDialogBuilder;
    private final int GET_IP_REQUEST = 42;
    private final int LOGIN_REQUEST = 27;
    private final String DEFAULT_RFID_ADDRESS = "127.000.000.001";
    private final String DEFAULT_RFID_PORT = "34000";
    private final int languageFlags[][]={
            /*
             *RBS Another trick to ensure proper UX. The flag stack will show all three countries no matter
             * what language is selected; The current language will be in front of the stack. This requires
             * keeping track of the current order of the flags to select the right language with getCurrentItem()
             */
        {R.mipmap.spain, R.mipmap.china, R.mipmap.united_kingdom},
        {R.mipmap.united_kingdom, R.mipmap.china, R.mipmap.spain},
        {R.mipmap.united_kingdom, R.mipmap.spain, R.mipmap.china}
    };

    public Settings() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        finalView = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsListView = (ListView) finalView.findViewById(R.id.settings_listview_configs);
        save = (ImageView) finalView.findViewById(R.id.settings_ImageView_save);
        calibration = (ImageView) finalView.findViewById(R.id.settings_ImageView_calibration);
        factoryReset = (ImageView) finalView.findViewById(R.id.settings_ImageView_factoryReset);
        tileConfig = (ImageView) finalView.findViewById(R.id.settings_ImageView_tiles);

        drawLanguageFlags();

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveSettings();
            }
        });
        calibration.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), LogInActivity.class);
                startActivityForResult(intent, LOGIN_REQUEST);
            }
        });
        factoryReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showResetDialog();
            }
        });
        tileConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity)getActivity()).switchToLayout(R.id.opt_tiles);
            }
        });

        currentSettings = new ArrayList<>();
        currentSettings.add(getMachineControllerAddress());
        listAdapter = new SettingsListAdapter(getContext(), currentSettings);
        settingsListView.setAdapter(listAdapter);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedItem = position;
                Intent intent = new Intent(getContext(), AskForIP.class);
                intent.putExtra("defaultIP", currentSettings.get(lastSelectedItem).address);
                intent.putExtra("defaultPort", currentSettings.get(lastSelectedItem).port);
                startActivityForResult(intent, GET_IP_REQUEST);
            }
        });

        return finalView;
    }

    public void saveSettings() {
        //Save config, and update current values.
        setMachineControllerAddress(currentSettings.get(0));
        ((MainActivity)getActivity()).updateServerAddress();

        //CHANGE LANGUAGE
        int currentLanguage = getLanguage();
        int selectedFlag = languageFlags[currentLanguage][mFlippableStack.getCurrentItem()];
        int selectedLanguage = 0;
        if(selectedFlag == R.mipmap.united_kingdom){
            selectedLanguage = 0;
        } else if (selectedFlag == R.mipmap.spain) {
            selectedLanguage = 1;
        } else if (selectedFlag == R.mipmap.china) {
            selectedLanguage = 2;
        }

        //If the language has changed, restart of the app is required.
        if(selectedLanguage != currentLanguage) {
            setLanguage(selectedLanguage);
            showLanguageDialog();
        }

        if(currentSettings.size() > 1) {
            //Save to server configuration
            try {
                JSONObject JSONOutput = new JSONObject();
                JSONArray RFIDPorts = new JSONArray();
                JSONArray RFIDAddresses = new JSONArray();
                JSONOutput.put("command_ID", "SCFG");
                //Set PLC configuration
                JSONOutput.put("PLC_address", currentSettings.get(1).address);
                //Set RFID configuration
                for (int i = 2; i < currentSettings.size(); i++) {
                    if(!currentSettings.get(i).port.equals(""))
                        RFIDPorts.put(Integer.parseInt(currentSettings.get(i).port));
                    else
                        RFIDPorts.put(0);
                    RFIDAddresses.put(currentSettings.get(i).address);
                }
                JSONOutput.put("RFID_ports", RFIDPorts);
                JSONOutput.put("RFID_addresses", RFIDAddresses);
                mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");

            } catch (JSONException exc) {
                Log.d("JSON exception", exc.getMessage());
            }
        }

        Toast.makeText(getActivity().getBaseContext(), "Configuration saved", Toast.LENGTH_SHORT).show(); //RBS TODO STRINGS
    }

    /*
     * To ensure that the local settings are synchronized with the settings
     * stored in the machine, these will be enquired before allowing the user
     * to make any changes.
     */
    public void retrieveSettings() {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("command_ID", "GCFG");
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    public void onSettingsRetrieved(String CMD) {
        try {
            JSONObject JSONparser = new JSONObject(CMD);
            int totalRFIDServers = JSONparser.getInt("RFID_servers");
            String PLCAddress = JSONparser.getString("PLC_address");
            JSONArray RFIDServers = JSONparser.getJSONArray("RFID_addresses"); //This string is another JSON containing both, ip and port.
            JSONArray RFIDPorts = JSONparser.getJSONArray("RFID_ports"); //This string is another JSON containing both, ip and port.
            //Load these settings into an array
            currentSettings.clear();
            currentSettings.add(getMachineControllerAddress());
            currentSettings.add(new IPsetting("PLC Address", PLCAddress, "00102"));
            for(int i=0; i<totalRFIDServers; i++) {
                if(i<RFIDPorts.length())
                    currentSettings.add(new IPsetting("RFID server " + (i + 1), RFIDServers.getString(i), RFIDPorts.getString(i)));
                else
                    currentSettings.add(new IPsetting("RFID server " + (i + 1), "", ""));
            }
            listAdapter.notifyDataSetChanged();

        } catch (Exception jsonExc) {
            Log.e("JSON Exception", "onSettingsRetrieved():" + jsonExc.getMessage());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IP_REQUEST) {
            if(resultCode == RESULT_OK) {
                currentSettings.get(lastSelectedItem).port = data.getStringExtra("port");
                currentSettings.get(lastSelectedItem).address = data.getStringExtra("ip");
                listAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == LOGIN_REQUEST) {
            if(resultCode == RESULT_OK) {
                if(data.getStringExtra("authentication").equals("ok")) {
                    Log.d("Authentication", "successful");
                    ((MainActivity)getActivity()).switchToLayout(R.id.opt_calibration);
                } else {
                    Log.d("Authentication", "unsuccessful");
                }
            }
        }
    }

    private void drawLanguageFlags() {
        fragmentList = new ArrayList<>();

        int currentLanguage = getLanguage();
        for(int i=0; i<3; i++) {
            //Create in appropriate order
            fragmentList.add(ColorFragment.newInstancePicture(languageFlags[currentLanguage][i]));
        }

        fragmentAdapter = new ColorFragmentAdapter(getFragmentManager(), fragmentList);
        mFlippableStack = (FlippableStackView) finalView.findViewById(R.id.language_stack);
        mFlippableStack.initStack(3, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(fragmentAdapter);
    }

    private class ColorFragmentAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public ColorFragmentAdapter( FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage("Please restart the APP to change the language\n(请重新启动应用程序以更改语言)");
        AlertDialog dialog = builder.create();
        dialog.setIcon(R.mipmap.warning);
        BiggerDialogs.show(dialog);
    }

    private void showResetDialog() {
        resetDialogBuilder = new AlertDialog.Builder(getActivity());
        resetDialogBuilder.setTitle(getString(R.string.Warning));
        resetDialogBuilder.setMessage("All settings will be lost"); //TODO RBS STRINGS
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //i=1: do not delete server address.
                        int listSize = currentSettings.size();
                        for(int i=1; i<listSize; i++) {
                            currentSettings.get(i).address = "000.000.000.000";
                            currentSettings.get(i).port = "";
                        }
                        listAdapter.notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEUTRAL:
                        break;
                }
            }
        };
        resetDialogBuilder.setPositiveButton("Continue", dialogClickListener); //TODO RBS STRINGS
        resetDialogBuilder.setNeutralButton(getString(R.string.Cancel), dialogClickListener);
        AlertDialog dialog = resetDialogBuilder.create();
        dialog.setIcon(R.mipmap.warning);
        BiggerDialogs.show(dialog);
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof Settings.OnFragmentInteractionListener) {
            mFragmentInteraction = (Settings.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }

    public void whenEnteringFragment() {
        retrieveSettings();
    }

    public void whenLeavingFragment() {

        //show menu
        // saveSettings();
    }

    /*
     * Delete list of settings when disconnecting from the server.
     * This will avoid conflicts. Remove everything except server IP
     */
    public void onLostConnection() {
        while(currentSettings.size() > 1) {
            currentSettings.remove(currentSettings.size()-1);
        }
        listAdapter.notifyDataSetChanged();
    }

    public void onEstablishedConnection() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                //Run 500ms after in case the server is just started
                //and not ready yet for answering commands (it happens.)
                //RBS TODO yes, not the most elegant solution...
                retrieveSettings();
            }
        }, 500);
    }

}
