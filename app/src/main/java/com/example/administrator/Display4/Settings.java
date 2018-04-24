package com.example.administrator.Display4;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;
import java.util.ArrayList;
import java.util.List;


public class Settings extends Fragment {

    private FlippableStackView mFlippableStack;
    private ColorFragmentAdapter mPageAdapter;
    private List<Fragment> mViewPagerFragments;
    private int currentLanguage;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPref_editor;
    private View finalView;
    private EditText editText_PC_IP_Address_1;
    private EditText editText_PC_IP_Address_2;
    private EditText editText_PC_IP_Address_3;
    private EditText editText_PC_IP_Address_4;
    private EditText editText_PC_IP_Address_Port;
    private ImageView save;
    /*
     *RBS Another trick to ensure proper UX. The flag stack will show all three countries no matter
     * what language is selected; The current language will be in front of the stack. This requires
     * keeping track of the current order of the flags to select the right language with getCurrentItem()
     */
    private int languageFlags[][]={
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

        //GUI
        editText_PC_IP_Address_1 = (EditText) finalView.findViewById(R.id.editText_PC_IP_Address_1);
        editText_PC_IP_Address_2 = (EditText) finalView.findViewById(R.id.editText_PC_IP_Address_2);
        editText_PC_IP_Address_3 = (EditText) finalView.findViewById(R.id.editText_PC_IP_Address_3);
        editText_PC_IP_Address_4 = (EditText) finalView.findViewById(R.id.editText_PC_IP_Address_4);
        editText_PC_IP_Address_Port = (EditText) finalView.findViewById(R.id.editText_PC_IP_Address_Port);
        save = (ImageView) finalView.findViewById(R.id.settings_ImageView_save);


        //Read current language
        sharedPref = getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        sharedPref_editor = sharedPref.edit();
        currentLanguage = sharedPref.getInt("LANGUAGE", 0);
        createViewPagerFragments();
        mPageAdapter = new ColorFragmentAdapter(getFragmentManager(), mViewPagerFragments);
        mFlippableStack = (FlippableStackView) finalView.findViewById(R.id.language_stack);
        mFlippableStack.initStack(3, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(mPageAdapter);

        //Set fields to current saved values
        String SERVER_IP = sharedPref.getString(getString(R.string.Stored_PC_IP_Address), "127.0.0.1");
        String SERVER_PORT = sharedPref.getString(getString(R.string.Stored_PC_IP_Port), "24601");
        String Server_IP_split[] = SERVER_IP.split("[.]+");
        editText_PC_IP_Address_1.setText(Server_IP_split[0]);
        editText_PC_IP_Address_2.setText(Server_IP_split[1]);
        editText_PC_IP_Address_3.setText(Server_IP_split[2]);
        editText_PC_IP_Address_4.setText(Server_IP_split[3]);
        editText_PC_IP_Address_Port.setText(SERVER_PORT);

        //Save configuration
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveSettings();
            }
        });

        return finalView;
    }

    public void saveSettings() {
        //Save PORT
        sharedPref_editor.putString(getString(R.string.Stored_PC_IP_Port), editText_PC_IP_Address_Port.getText().toString());
        sharedPref_editor.commit();

        //Save IP address
        sharedPref_editor.putString(getString(R.string.Stored_PC_IP_Address),
                    editText_PC_IP_Address_1.getText().toString()+"."+
                        editText_PC_IP_Address_2.getText().toString()+"."+
                        editText_PC_IP_Address_3.getText().toString()+"."+
                        editText_PC_IP_Address_4.getText().toString());
        sharedPref_editor.commit();

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

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.Warning));
        builder.setMessage("Please restart the APP to change the language\n(请重新启动应用程序以更改语言)");
        AlertDialog dialog = builder.create();
        dialog.setIcon(R.mipmap.warning);
        dialog.show();
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void createViewPagerFragments() {
        mViewPagerFragments = new ArrayList<>();

        //Add flags to stack in the appropriate order
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
}
