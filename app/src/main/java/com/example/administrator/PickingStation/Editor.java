package com.example.administrator.PickingStation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.administrator.PickingStation.BrickManager.getRaw;


public class Editor extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FlippableStackView mFlippableStack;
    private List<Fragment> mViewPagerFragments;
    private ColorFragmentAdapter mPageAdapter;
    final ToggleButton PalletButton[]=new ToggleButton[10+1];
    private ListView editor_listView_colours;
    private ListView editor_listView_grades;
    private View view;
    private Button editor_button_add;
    private Button editor_button_delete;
    private Button editor_button_format;
    private Button editor_button_edit;
    private TextView editor_textView_Indicator;

    public Editor() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        view = inflater.inflate(R.layout.fragment_editor, container, false);
        editor_listView_colours = (ListView) view.findViewById(R.id.editor_listView_colours);
        editor_listView_grades = (ListView) view.findViewById(R.id.editor_listView_grades);
        editor_button_add = (Button) view.findViewById(R.id.editor_button_add);
        editor_button_delete = (Button) view.findViewById(R.id.editor_button_delete);
        editor_button_format = (Button) view.findViewById(R.id.editor_button_format);
        editor_button_edit = (Button) view.findViewById(R.id.editor_button_edit);
        editor_textView_Indicator=(TextView) view.findViewById(R.id.editor_textView_Indicator);

        mViewPagerFragments = new ArrayList<>();

        editor_listView_colours.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.getFocusables(position);
                view.setSelected(true);
            }
        });
        editor_listView_grades.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
            }
        });


/*
        mSwipeRefreshLayout=(SwipeRefreshLayout) view.findViewById(R.id.editor_SwipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int index=1;
                for (int j = 1; j < PalletButton.length; j++) {
                    if (PalletButton[j].isChecked()) {
                        index = j;
                        break;
                    }
                }
                askForPalletContents(index);
            }
        });
        */

        mPageAdapter = new ColorFragmentAdapter(getFragmentManager(), mViewPagerFragments);
        mFlippableStack = (FlippableStackView) view.findViewById(R.id.editor_flipview_bricks_stack);
        mFlippableStack.initStack(8, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(mPageAdapter);
        mFlippableStack.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            public void onPageSelected( int position ) {
                editor_textView_Indicator.setText(Integer.toString(position) + " / " + Integer.toString(mPageAdapter.getCount()-1)
                        +"  ("+Integer.toString(mPageAdapter.getCount()-position)+")");
                if(position==0&&mPageAdapter.getCount()>1){
                    mFlippableStack.setCurrentItem(1);
                }
            }

            public void onPageScrollStateChanged( int state ) {

            }

            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
            }
        });

        for(int i=1; i<PalletButton.length; i++) {
            String buttonID = "editor_button_pallet_" + (i);
            int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());

            PalletButton[i] = ((ToggleButton) view.findViewById(resID));
            PalletButton[i].setOnClickListener(new View.OnClickListener() {
                public void onClick( View v ) {
                    int index = 1;
                    //When pressing on a pallet, uncheck everyButton
                    for (int j = 1; j < PalletButton.length; j++) {
                        if (PalletButton[j].getId() == v.getId()) {
                            index = j;
                        }
                        PalletButton[j].setChecked(false);
                    }
                    //And check that button
                    PalletButton[index].setChecked(true);
                    askForPalletContents(index);
                }
            });
        }

        editor_button_add.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                int type = getRaw(editor_listView_grades.getCheckedItemPosition(), editor_listView_colours.getCheckedItemPosition());
                addBrick(getSelectedPallet(), type);
                askForPalletContents(getSelectedPallet());
            }

        });

        editor_button_delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                deleteBrick(getSelectedPallet(),  mFlippableStack.getCurrentItem());
                askForPalletContents(getSelectedPallet());
            }

        });

        editor_button_format.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                formatPallet(getSelectedPallet());
                askForPalletContents(getSelectedPallet());
            }

        });

        editor_button_edit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                editBrick(getSelectedPallet(), mFlippableStack.getCurrentItem(), editor_listView_grades.getCheckedItemPosition(), editor_listView_colours.getCheckedItemPosition());
                askForPalletContents(getSelectedPallet());
            }
        });


        return view;
    }

    private int getSelectedPallet() {
        int index = 1;
        for (int j = 1; j < PalletButton.length; j++) {
            if (PalletButton[j].isChecked()) {
                index = j;
                break;
            }
        }
        return index;
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }

    private void askForPalletContents(int palletNumber) {
        try {
            JSONObject RGMVCommand = new JSONObject();
            RGMVCommand.put("command_ID", "RGMV");
            RGMVCommand.put("palletNumber", palletNumber);
            mListener.onSendCommand(RGMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    private void deleteBrick(int palletNumber, int position) {
        try {
            JSONObject RDMVCommand = new JSONObject();
            RDMVCommand.put("command_ID", "RDMV");
            RDMVCommand.put("positionToDelete", position);
            RDMVCommand.put("selectedPallet", palletNumber);
            mListener.onSendCommand(RDMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    private void formatPallet(int palletNumber) {
        try {
            JSONObject RFMVCommand = new JSONObject();
            RFMVCommand.put("command_ID", "RFMV");
            RFMVCommand.put("selectedPallet", palletNumber);
            mListener.onSendCommand(RFMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    private void addBrick(int palletNumber, int type) {
        try {
            JSONObject RAMVCommand = new JSONObject();
            RAMVCommand.put("command_ID", "RAMV");
            RAMVCommand.put("selectedPallet", palletNumber);
            RAMVCommand.put("valueToAdd", type);
            mListener.onSendCommand(RAMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    private void editBrick(int palletNumber, int positionToEdit, int newGrade, int newColor) {
        try {
            JSONObject REMVCommand = new JSONObject();
            REMVCommand.put("command_ID", "REMV");
            REMVCommand.put("palletNumber", palletNumber);
            REMVCommand.put("positionToEdit", positionToEdit);
            REMVCommand.put("newGrade", newGrade);
            REMVCommand.put("newColor", newColor);
            mListener.onSendCommand(REMVCommand.toString());
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    public void onTcpReply(String mMessage) {
        //This might be void under certain circumstances RBS: should be fixed, then
        if((mMessage.length() != 0) && (view != null) && (PalletButton != null)) {
            updateViewPagerFragments(mMessage);
            int index = 1;
            try {
                JSONObject JSONparser = new JSONObject(mMessage);
                index = JSONparser.getInt("palletNumber");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }
            //Set Button
            for (int j = 1; j < PalletButton.length; j++) {
                //uncheck buttons
                PalletButton[j].setChecked(false);
            }
            PalletButton[index].setChecked(true);
        }
    }

    private void updateViewPagerFragments(String JSONData){
        if(mViewPagerFragments!=null) {
            mViewPagerFragments.clear();
            mViewPagerFragments.add(ColorFragment.newInstancePicture(R.mipmap.pallet));
            mPageAdapter.notifyDataSetChanged();
        }
        try {
            JSONObject JSONparser = new JSONObject(JSONData);
            JSONArray values = JSONparser.getJSONArray("memoryValues");
            int totalBricks = JSONparser.getInt("totalBricks");

            for (int i = 0; i < totalBricks; i++) {
                int currentBrickRawType = values.getInt(i);
                mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(BrickManager.getColorFromRaw(currentBrickRawType), BrickManager.getGradeFromRaw(currentBrickRawType)));
            }

        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }

        mPageAdapter.notifyDataSetChanged();
        mFlippableStack.setCurrentItem(mPageAdapter.getCount()-1);
    }

    private class ColorFragmentAdapter extends FragmentStatePagerAdapter {
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

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }
    }

    public void whenEnteringFragment() {

    }

    public void whenLeavingFragment() {

    }
}
