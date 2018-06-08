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

import java.util.ArrayList;
import java.util.List;


public class Editor extends Fragment {

    private OnFragmentInteractionListener mListener;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FlippableStackView mFlippableStack;
    private List<Fragment> mViewPagerFragments;
    private ColorFragmentAdapter mPageAdapter;

    final ToggleButton PalletButton[]=new ToggleButton[10+1];
    //This view. RBS. And yes--it's not final.
    View finalview;

    public Editor() {
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    public void  onTcpReply(String mMessage) {
        //This might be void under certain circumstances
        if((mMessage.length() != 0) && (finalview != null) && (PalletButton != null)) {
            updateViewPagerFragments(mMessage);
            int index = Integer.parseInt(mMessage.substring(5,7));
            //Set Button
            for (int j = 1; j < PalletButton.length; j++) {
                //uncheck buttons
                PalletButton[j].setChecked(false);
            }
            final TextView editor_textView_Indicator2 = (TextView) finalview.findViewById(R.id.editor_textView_Indicator2);
            editor_textView_Indicator2.setText("Pallet " + Integer.toString(index + 1));
            PalletButton[index + 1].setChecked(true);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        //RBS change name
        finalview = view;
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
                mListener.onSendCommand("RGMV_"+ String.format("%02d",index-1));
            }
        });*/

        ////////////////////////////////////////
        // PALLET OVERVIEW
        ////////////////////////////////////////

        createViewPagerFragments();

        mPageAdapter = new ColorFragmentAdapter(getFragmentManager(), mViewPagerFragments);
        mFlippableStack = (FlippableStackView) view.findViewById(R.id.editor_flipview_bricks_stack);
        mFlippableStack.initStack(8, StackPageTransformer.Orientation.VERTICAL, (float)1,(float)0.5,(float)0,StackPageTransformer.Gravity.BOTTOM);
        mFlippableStack.setAdapter(mPageAdapter);
        final TextView editor_textView_Indicator=(TextView) view.findViewById(R.id.editor_textView_Indicator);


        mFlippableStack.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            public void onPageSelected( int position ) {
                editor_textView_Indicator.setText(Integer.toString(position) + " / " + Integer.toString(mPageAdapter.getCount()-1)
                        +"  ("+Integer.toString(mPageAdapter.getCount()-position)+")");
                if(position==0&&mPageAdapter.getCount()>1){
                    mFlippableStack.setCurrentItem(1);
                }
            }


            public void onPageScrollStateChanged( int state ) {
                //dummytextbox.setText(state);
            }

            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
                //dummytextbox.setText(position);
            }
        });

        ////////////////////////////////////////
        // PALLET BUTTONS
        ////////////////////////////////////////
        for(int i=1; i<PalletButton.length; i++) {
                String buttonID = "editor_button_pallet_" + (i);

                int resID = getResources().getIdentifier(buttonID, "id", getActivity().getPackageName());
                PalletButton[i] = ((ToggleButton) view.findViewById(resID));
                PalletButton[i].setOnClickListener(new View.OnClickListener() {
                    public void onClick( View v ) {

                        int index = 1;
                        for (int j = 1; j < PalletButton.length; j++) {
                            if (PalletButton[j].getId() == v.getId()) {
                                index = j;
                            }
                            //PalletButton[j].setChecked(false);
                        }
                        //CODE MESSAGE HERE
                        PalletButton[index].setChecked(true);
                        String mSend;
                        mSend="RGMV_"+ String.format("%02d",index-1);
                        mListener.onSendCommand(mSend);
                    }
                });
            }

        ////////////////////////////////////////
        // ADD BUTTON
        ////////////////////////////////////////

        final Button editor_button_add = (Button) finalview.findViewById(R.id.editor_button_add);

        editor_button_add.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                String mSend;
                int index = 1;
                for (int j = 1; j < PalletButton.length; j++) {
                    if (PalletButton[j].isChecked()) {
                        index = j;
                        break;
                    }
                }

                mSend="RAMV_"+ String.format("%02d",index-1)+ "_";
                ListView editor_listView_colours = (ListView) finalview.findViewById(R.id.editor_listView_colours);
                ListView editor_listView_grades = (ListView) finalview.findViewById(R.id.editor_listView_grades);

                int mToAdd= ((editor_listView_grades.getCheckedItemPosition()+1)<<4)+ editor_listView_colours.getCheckedItemPosition()+1 ;

                mSend+=Character.toString((char) mToAdd);

                mListener.onSendCommand(mSend);
                mListener.onSendCommand("RGMV_"+ String.format("%02d",index-1));
            }

        });
        ////////////////////////////////////////
        // Delete BUTTON
        ////////////////////////////////////////

        final Button editor_button_delete = (Button) finalview.findViewById(R.id.editor_button_delete);

        editor_button_delete.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                String mSend;
                int index = 1;
                for (int j = 1; j < PalletButton.length; j++) {
                    if (PalletButton[j].isChecked()) {
                        index = j;
                        break;
                    }
                }

                mSend="RDMV_"+ String.format("%02d",index-1)+ "_";
                ListView editor_listView_colours = (ListView) finalview.findViewById(R.id.editor_listView_colours);
                ListView editor_listView_grades = (ListView) finalview.findViewById(R.id.editor_listView_grades);

                int mPositionToDelete=mFlippableStack.getCurrentItem();

                mSend+=String.format("%03d",mPositionToDelete);

                mListener.onSendCommand(mSend);
                mListener.onSendCommand("RGMV_"+ String.format("%02d",index-1));
            }

        });

        ////////////////////////////////////////
        // FORMAT BUTTON
        ////////////////////////////////////////

        final Button editor_button_format = (Button) finalview.findViewById(R.id.editor_button_format);

        editor_button_format.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                String mSend;
                int index = 1;
                for (int j = 1; j < PalletButton.length; j++) {
                    if (PalletButton[j].isChecked()) {
                        index = j;
                        break;
                    }
                }

                mSend="RFMV_"+ String.format("%02d",index-1);
                mListener.onSendCommand(mSend);
                mListener.onSendCommand("RGMV_"+ String.format("%02d",index-1));
            }

        });


        ////////////////////////////////////////
        // COLOUR LISTVIEW
        ////////////////////////////////////////

        final ListView editor_listView_colours=(ListView) view.findViewById(R.id.editor_listView_colours);

        editor_listView_colours.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {

                view.getFocusables(position);
                view.setSelected(true);

            }
        });

        final ListView editor_listView_grades=(ListView) view.findViewById(R.id.editor_listView_grades);
        editor_listView_grades.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                view.setSelected(true);
            }
        });


        //PalletButton[1].setChecked(true);
        //mListener.onSendCommand("RGMV_00");

        editor_listView_colours.setSelection(1);
        editor_listView_grades.setSelection(1);
        //editor_listView_colours.getAdapter().notifyA();
        return view;
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


    public void updateViewPagerFragments(String memory){
        if(mViewPagerFragments!=null) {
                //while(mViewPagerFragments.size()>0){

                //}
            mViewPagerFragments.clear();
            mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(R.color.colorPrimaryPale),"Pallet"));

            mPageAdapter.notifyDataSetChanged();
            //ViewPagerFragments.setSaveFromParentEnabled(false);
        }
        Log.d("MemoryValue", "Memory Value: " + memory );
        int Number=(int) memory.charAt(8);
        Log.d("MemoryValue", "Memory raw nob: " + Number );
             for (int i = 0; i < Number - 17; i++) {
                int value = (int) memory.charAt(9 + i);
                 Log.d("MemoryValue", "Memory raw b: " + value );
                int colorID = getResources().getIdentifier("brick_color_" + (value & 15), "color", getContext().getPackageName());
                String grade = getResources().getString(getResources().getIdentifier("brick_grade_" + (value >> 4), "string", getContext().getPackageName()));
                mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(colorID),grade));
            }




        mPageAdapter.notifyDataSetChanged();
        mFlippableStack.setCurrentItem(mPageAdapter.getCount()-1);

        mSwipeRefreshLayout.setRefreshing(false);
    }
    private void createViewPagerFragments() {
        mViewPagerFragments = new ArrayList<>();
        //int startColor1 = getResources().getColor(R.color.colorPrimaryPale);
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_orange_dark),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_green_light),"Grade B"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_orange_dark),"Grade C"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_green_light),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_green_light),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_red_light),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_red_light),"Grade B"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_orange_dark),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_red_light),"Grade B"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_green_light),"Grade A"));
        //mViewPagerFragments.add(ColorFragment.newInstanceOnlyBackground(getResources().getColor(android.R.color.holo_green_light),"Grade A"));
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
}
