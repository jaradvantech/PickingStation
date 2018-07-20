package com.example.administrator.PickingStation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bartoszlipinski.flippablestackview.FlippableStackView;
import com.bartoszlipinski.flippablestackview.StackPageTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.administrator.PickingStation.BrickManager.getRaw;
import static com.example.administrator.PickingStation.Commands.RPRV;


public class Editor extends Fragment {

    private OnFragmentInteractionListener mListener;
    private FlippableStackView mFlippableStack;
    private List<Fragment> mViewPagerFragments;
    private ColorFragmentAdapter mPageAdapter;
    private TextView tops[];
    private ImageView pallets[];
    private ImageView selectedShadow[];
    private ListView editor_listView_colours;
    private ListView editor_listView_grades;
    private View view;
    private LayoutInflater inflater;
    private ViewGroup container;
    private LinearLayout holder;
    private int armNumber;
    private int palletNumber;
    private int selectedPallet = 1;
    private Button editor_button_add;
    private Button editor_button_delete;
    private Button editor_button_format;
    private Button editor_button_edit;
    private TextView editor_textView_Indicator;
    private final String NULL_UID = "0000000000000000";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final int RPRV_PERIOD = 600;
    private boolean autoUpdate;

    public Editor() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        view = inflater.inflate(R.layout.fragment_editor, container, false);
        this.holder = view.findViewById(R.id.editor_linearLayout_holder);
        this.inflater = inflater;
        this.container = container;

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

        //setManipulatorNumber(SettingManager.getArms());
        setManipulatorNumber(7);

        mPageAdapter = new ColorFragmentAdapter(getFragmentManager(), mViewPagerFragments);
        mFlippableStack = (FlippableStackView) view.findViewById(R.id.editor_flipview_bricks_stack);
        mFlippableStack.initStack(8, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(mPageAdapter);
        mFlippableStack.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            public void onPageSelected( int position ) {
                editor_textView_Indicator.setText("Brick "+Integer.toString(position)+" of "+Integer.toString(mPageAdapter.getCount()-1));
                if(position==0&&mPageAdapter.getCount()>1){
                    mFlippableStack.setCurrentItem(1);
                }
            }

            public void onPageScrollStateChanged( int state ) {

            }

            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
            }
        });

        editor_button_add.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                int type = getRaw(editor_listView_grades.getCheckedItemPosition(), editor_listView_colours.getCheckedItemPosition());
                addBrick(selectedPallet, type);
                askForPalletContents(selectedPallet);
            }

        });

        editor_button_delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                deleteBrick(selectedPallet,  mFlippableStack.getCurrentItem());
                askForPalletContents(selectedPallet);
            }

        });

        editor_button_format.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                formatPallet(selectedPallet);
                askForPalletContents(selectedPallet);
            }

        });

        editor_button_edit.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                editBrick(selectedPallet, mFlippableStack.getCurrentItem(), editor_listView_grades.getCheckedItemPosition(), editor_listView_colours.getCheckedItemPosition());
                askForPalletContents(selectedPallet);
            }
        });


        return view;
    }

    public void setManipulatorNumber(int number) {
        this.armNumber = number;
        this.palletNumber = number*2;
        tops = new TextView[palletNumber];
        selectedShadow = new ImageView[palletNumber];
        pallets = new ImageView[palletNumber];

        holder.removeAllViews();
        int currentPallet = 0;
        for(int i=0; i<armNumber; i++) {
            View currentView = inflater.inflate(R.layout.editor_segment, container, false);
            tops[currentPallet] = currentView.findViewById(R.id.editorSegment_textView_topDown);
            selectedShadow[currentPallet] = currentView.findViewById(R.id.editorSegment_imageView_selectedDown);
            pallets[currentPallet] = currentView.findViewById(R.id.editorSegment_imageView_palletDown);
            TextView text = currentView.findViewById(R.id.editorSegment_textView_down);
            text.setText("Pallet " + (currentPallet+1));
            currentPallet++;

            tops[currentPallet] = currentView.findViewById(R.id.editorSegment_textView_topUp);
            selectedShadow[currentPallet] = currentView.findViewById(R.id.editorSegment_imageView_selectedUp);
            pallets[currentPallet] = currentView.findViewById(R.id.editorSegment_imageView_palletUp);
            text = currentView.findViewById(R.id.editorSegment_textView_up);
            text.setText("Pallet " + (currentPallet+1));
            currentPallet++;
            holder.addView(currentView);
        }

        for(int i=0; i<palletNumber; i++) {
            selectedShadow[i].setVisibility(View.INVISIBLE);
        }

        for(int i=0; i<palletNumber; i++) {
            final int fi = i;
            tops[i].setOnClickListener(new View.OnClickListener() {
                public void onClick( View v ) {
                    for (int j=0; j<palletNumber; j++) {
                        selectedShadow[j].setVisibility(View.INVISIBLE);
                    }
                    //And check that button
                    selectedShadow[fi].setVisibility(View.VISIBLE);
                    selectedPallet = fi+1;
                    askForPalletContents(selectedPallet);
                }
            });
        }
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
        if((mMessage.length() != 0) && (view != null) && (tops != null)) {
            updateViewPagerFragments(mMessage);
            int index = 1;
            try {
                JSONObject JSONparser = new JSONObject(mMessage);
                index = JSONparser.getInt("palletNumber");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }
            //Set Button
            for (int j = 0; j < tops.length; j++) {
                selectedShadow[j].setVisibility(View.INVISIBLE);
            }
            selectedShadow[index-1].setVisibility(View.VISIBLE); //pallets start at 1 serverside
            selectedPallet = index;
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

    public void updateBrickInfo(String CMD) {
        try {
            JSONObject JSONparser = new JSONObject(CMD);
            JSONArray palletInformation = JSONparser.getJSONArray("palletInformation");
            int NumberOfPallets = palletInformation.length();

            //If this happens, that means we are connected to a machine that has a different number
            //of manipulators, and the screen has to be redrawn before continuing
            if(NumberOfPallets != palletNumber) {
                Log.e("Editor", "Number of pallets has changed from " + palletNumber + " to " + NumberOfPallets + "!!!!" );
                SettingManager.setArms(NumberOfPallets/2);
                this.setManipulatorNumber(NumberOfPallets/2);
            }

            for(int i=0; i<palletNumber; i++) {
                String palletUID = NULL_UID;
                int numberOfBricks=0, topBrick=0;
                try {
                    palletUID = palletInformation.getJSONObject(i).getString("palletUID");
                    topBrick = palletInformation.getJSONObject(i).getInt("topBrick");
                    numberOfBricks = palletInformation.getJSONObject(i).getInt("numberOfBricks");
                } catch (Exception jsonExc) {
                    Log.e("JSON Exception", "updateBricksInPallets(): " + jsonExc.getMessage());
                }


                if (palletUID.contentEquals(NULL_UID)) {
                    pallets[i].setVisibility(View.INVISIBLE);
                    tops[i].setBackgroundColor(Color.TRANSPARENT);
                    tops[i].setText("");
                } else {
                    pallets[i].setVisibility(View.VISIBLE);
                    tops[i].setVisibility(View.VISIBLE);
                    if (numberOfBricks > 0) {
                        tops[i].setText(Integer.toString(numberOfBricks));
                        int colorID = getResources().getIdentifier("brick_color_" + (topBrick & 15), "color", getContext().getPackageName());
                        String grade = getResources().getString(getResources().getIdentifier("brick_grade_" + (topBrick >> 4), "string", getContext().getPackageName()));

                        GradientDrawable gd = new GradientDrawable();
                        gd.setColor(getResources().getColor(colorID));
                        gd.setCornerRadius(1);
                        gd.setStroke(2, 0xFF000000);
                        tops[i].setBackground(gd);
                    } else {
                        tops[i].setBackgroundColor(Color.TRANSPARENT);
                        tops[i].setText("");
                    }
                }
            }


        } catch (Exception jsonExc) {
            Log.e("JSON Exception", "Editor: updateLineBrickInfo(): " + jsonExc.getMessage());
        }
    }

    final Runnable timer_editor = new Runnable() {
        @Override
        public void run() {
            mListener.onSendCommand(RPRV);
            if(autoUpdate == false) handler.removeCallbacksAndMessages(null);
            else handler.postDelayed(this, RPRV_PERIOD);
        }
    };

    public void whenEnteringFragment() {
        handler.postDelayed(timer_editor, RPRV_PERIOD);
        autoUpdate=true;
    }

    public void whenLeavingFragment() {
        autoUpdate = false;
    }
}
