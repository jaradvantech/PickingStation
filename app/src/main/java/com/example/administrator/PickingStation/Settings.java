package com.example.administrator.PickingStation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.administrator.PickingStation.SettingManager.getFromPreferences;
import static com.example.administrator.PickingStation.SettingManager.loadDefaults;
import static com.example.administrator.PickingStation.SettingManager.previousConfigurationExists;
import static com.example.administrator.PickingStation.SettingManager.saveToPreferences;


public class Settings extends Fragment {

    private int lastSelectedItem = 0;
    private FlippableStackView mFlippableStack;
    private ColorFragmentAdapter mPageAdapter;
    private List<Fragment> mViewPagerFragments;
    private ArrayList<SettingObject> settingArrayList;
    private ListView settingsListView;
    private SettingsListAdapter listAdapter;
    private int currentLanguage;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPref_editor;
    private View finalView;
    private ImageView save;
    private ImageView calibration;
    private ImageView factoryReset;
    private static final int GET_IP_REQUEST = 42;
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

        if(!previousConfigurationExists(getActivity())) {
            loadDefaults(getActivity());
        }
        fillSettings();

        //Read current language
        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sharedPref_editor = sharedPref.edit();
        currentLanguage = sharedPref.getInt("LANGUAGE", 0);
        createViewPagerFragments();
        mPageAdapter = new ColorFragmentAdapter(getFragmentManager(), mViewPagerFragments);
        mFlippableStack = (FlippableStackView) finalView.findViewById(R.id.language_stack);
        mFlippableStack.initStack(3, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(mPageAdapter);


        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveSettings();
            }
        });
        calibration.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        factoryReset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadDefaults(getContext());
                fillSettings();
                saveSettings();
            }
        });

        return finalView;
    }

    public void saveSettings() {
        //Save config, and update current values.
        saveToPreferences(settingArrayList, getContext());
        ((MainActivity)getActivity()).updateServerAddress();

        //CHANGE LANGUAGE
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
            sharedPref_editor.putInt("LANGUAGE", selectedLanguage);
            sharedPref_editor.commit();
            showDialog();
        }
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void fillSettings() {
        settingArrayList = getFromPreferences(getContext());
        listAdapter = new SettingsListAdapter(getContext(), settingArrayList);
        settingsListView.setAdapter(listAdapter);

        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastSelectedItem = position;
                if(settingArrayList.get(lastSelectedItem).type.equals("ip")) {
                    Intent intent = new Intent(getContext(), AskForIP.class);
                    startActivityForResult(intent, GET_IP_REQUEST);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IP_REQUEST) {
            if(resultCode == RESULT_OK) {
                try {
                    JSONObject JSONOutput = new JSONObject();
                    JSONOutput.put("ip",  data.getStringExtra("ip"));
                    JSONOutput.put("port", data.getStringExtra("port"));
                    settingArrayList.get(lastSelectedItem).value = JSONOutput.toString();
                    listAdapter.notifyDataSetChanged();
                } catch(JSONException exc) {
                    Log.e("JSON exception", exc.getMessage());
                }
            }
        }
    }

    private void createViewPagerFragments() {
        //Add flags to stack in the appropriate order
        mViewPagerFragments = new ArrayList<>();
        for(int i=0; i<3; i++) {
            mViewPagerFragments.add(ColorFragment.newInstancePicture(languageFlags[currentLanguage][i]));
        }
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

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage("Please restart the APP to change the language\n(请重新启动应用程序以更改语言)");
        AlertDialog dialog = builder.create();
        dialog.setIcon(R.mipmap.warning);
        dialog.show();
    }
}
