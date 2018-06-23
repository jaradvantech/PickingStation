package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;



public class Manual extends Fragment {

    private OnFragmentInteractionListener mFragmentInteraction;
    private int currentArm = 1;
    private boolean[] manualMode = new boolean[5+1]; //Default = false. Will be inverted when sending the command so the default becomes "auto"
    private boolean[] VV = new  boolean[5+1]; //Length +1 so I can start at 1

    private ImageView manual_imageView_up;
    private ImageView manual_imageView_down;
    private ImageView manual_imageView_left;
    private ImageView manual_imageView_right;
    private ImageView manual_imageView_forward;
    private ImageView manual_imageView_backward;
    private ImageView manual_imageView_stop;

    public Manual() {
    }


    public static Manual newInstance( String param1, String param2 ) {
        Manual fragment = new Manual();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manual, container, false);

        Button manual_reset = (Button) view.findViewById(R.id.manual_reset);
        final Switch mode = view.findViewById(R.id.manual_switch_mode);
        ImageView manual_imageView_next = (ImageView) view.findViewById(R.id.manual_imageView_next);
        ImageView manual_imageView_previous = (ImageView) view.findViewById(R.id.debug_imageView_previous);

        ImageView manual_imageView_sucker = (ImageView) view.findViewById(R.id.manual_imageView_sucker);
        final ImageView manual_imageView_motor = (ImageView) view.findViewById(R.id.manual_imageView_motor);

        manual_imageView_up = (ImageView) view.findViewById(R.id.manual_imageView_up);
        manual_imageView_down = (ImageView) view.findViewById(R.id.manual_imageView_down);
        manual_imageView_left = (ImageView) view.findViewById(R.id.manual_imageView_left);
        manual_imageView_right = (ImageView) view.findViewById(R.id.manual_imageView_right);
        manual_imageView_forward = (ImageView) view.findViewById(R.id.manual_imageView_forward);
        manual_imageView_backward = (ImageView) view.findViewById(R.id.manual_imageView_backward);
        manual_imageView_stop = (ImageView) view.findViewById(R.id.manual_imageView_stop);


        final GifImageView manual_GifimageView_sucker_animation = (GifImageView) view.findViewById(R.id.manual_GifimageView_sucker_animation);
        final GifImageView manual_GifimageView_line_wheel = (GifImageView) view.findViewById(R.id.manual_GifimageView_line_wheel);


        final GifDrawable manual_GifimageView_sucker_animation_Drawable = (GifDrawable)manual_GifimageView_sucker_animation.getBackground();
        final GifDrawable manual_GifimageView_line_wheel_Drawable = (GifDrawable)manual_GifimageView_line_wheel.getBackground();

        final TextView currentArmText = (TextView) view.findViewById(R.id.debug_currentArm);
        final TextView manual_textView_line = (TextView) view.findViewById(R.id.manual_textView_line);


        manual_GifimageView_sucker_animation_Drawable.stop();
        manual_GifimageView_sucker_animation_Drawable.seekToFrame(0);
        manual_GifimageView_line_wheel_Drawable.stop();
        manual_GifimageView_line_wheel_Drawable.seekToFrame(0);


        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    manualMode[currentArm] = true;
                    writePLCData(0, 0, 0, currentArm);

                } else {
                    manualMode[currentArm] = false;
                    writePLCData(0, 0, 0, currentArm);
                }
            }
        });



        manual_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                //all to manual, all valves off
                for(int i=1; i<6; i++){
                    VV[i] = false;
                    manualMode[i] = true;
                    writePLCData(0, 0, 0, i);
                }

                //update switch
                mode.setChecked(manualMode[currentArm]);

                //update gif TODO simplify
                if(VV[currentArm] == true){
                    manual_GifimageView_sucker_animation_Drawable.reset();
                    manual_GifimageView_sucker_animation.setVisibility(View.VISIBLE);
                    manual_GifimageView_sucker_animation_Drawable.start();
                }else{
                    manual_GifimageView_sucker_animation_Drawable.stop();
                    manual_GifimageView_sucker_animation_Drawable.seekToFrame(0);
                    manual_GifimageView_sucker_animation.setVisibility(View.INVISIBLE);
                }
            }
        });

        manual_imageView_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if (currentArm > 1) {
                    currentArm--;
                    currentArmText.setText(Integer.toString(currentArm));

                    //Set mode switch to the appropiate value for this arm
                    mode.setChecked(manualMode[currentArm]);

                    if(VV[currentArm] == true){
                        manual_GifimageView_sucker_animation_Drawable.reset();
                        manual_GifimageView_sucker_animation.setVisibility(View.VISIBLE);
                        manual_GifimageView_sucker_animation_Drawable.start();
                    }else{
                        manual_GifimageView_sucker_animation_Drawable.stop();
                        manual_GifimageView_sucker_animation_Drawable.seekToFrame(0);
                        manual_GifimageView_sucker_animation.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        manual_imageView_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if (currentArm < 5) {
                    currentArm++;
                    currentArmText.setText(Integer.toString(currentArm));

                    mode.setChecked(manualMode[currentArm]);

                    if(VV[currentArm] == true){
                        manual_GifimageView_sucker_animation_Drawable.reset();
                        manual_GifimageView_sucker_animation.setVisibility(View.VISIBLE);
                        manual_GifimageView_sucker_animation_Drawable.start();
                    }else{
                        manual_GifimageView_sucker_animation_Drawable.stop();
                        manual_GifimageView_sucker_animation_Drawable.seekToFrame(0);
                        manual_GifimageView_sucker_animation.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        manual_imageView_sucker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                //onclick invert value
                VV[currentArm] = !VV[currentArm];
                //update image
                if(VV[currentArm]){
                    manual_GifimageView_sucker_animation_Drawable.reset();
                    manual_GifimageView_sucker_animation.setVisibility(View.VISIBLE);
                    manual_GifimageView_sucker_animation_Drawable.start();
                }else{
                    manual_GifimageView_sucker_animation_Drawable.stop();
                    manual_GifimageView_sucker_animation_Drawable.seekToFrame(0);
                    manual_GifimageView_sucker_animation.setVisibility(View.INVISIBLE);
                }

                writePLCData(0,0,0, currentArm);
            }
        });

        manual_imageView_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                writePLCData(0, 0, 0, currentArm);
                resetColorFilters();
            }
        });

        manual_imageView_motor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manual_GifimageView_line_wheel_Drawable.isPlaying()){
                    manual_GifimageView_line_wheel_Drawable.stop();
                    manual_textView_line.setText("Line stoped");
                    setLineMotor(false);

                }else{
                    manual_textView_line.setText("Line running!");
                    manual_GifimageView_line_wheel_Drawable.start();
                    setLineMotor(true);

                }
            }
        });

        //Send commands only if this arm is in manual mode, otherwise ignore click
        manual_imageView_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(1, 0, 0, currentArm);
            }
        });

        manual_imageView_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(2, 0, 0, currentArm);
            }
        });

        manual_imageView_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(0, 1, 0, currentArm);
            }
        });

        manual_imageView_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(0, 2, 0, currentArm);
            }
        });

        manual_imageView_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(0, 0, 1, currentArm);
            }
        });

        manual_imageView_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                if(manualMode[currentArm])
                    writePLCData(0, 0, 2, currentArm);
            }
        });


        return view;
    }

    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mFragmentInteraction = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentInteraction = null;
    }

    public void writePLCData(int MUD, int MLR, int MFB, int mArm) {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("command_ID", "PWDA");
            JSONOutput.put("selectedArm", mArm);
            JSONOutput.put("MM", manualMode[mArm]); //RBS this ! might be needed again
            JSONOutput.put("VV", VV[mArm]);
            JSONOutput.put("MFB", MFB);
            JSONOutput.put("MLR", MLR);
            JSONOutput.put("MUD", MUD);

            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    void serverResponse(String mResponse){
        //Tint icons as feedback
        try {
            JSONObject JSONparser = new JSONObject(mResponse);
            int MFBFeedback = JSONparser.getInt("MFBFeedback");
            int MLRFeedback = JSONparser.getInt("MLRFeedback");
            int MUDFeedback = JSONparser.getInt("MUDFeedback");

            if(MFBFeedback == 1) {
                resetColorFilters();
                manual_imageView_forward.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

            } else if (MFBFeedback == 2){
                resetColorFilters();
                manual_imageView_backward.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

            } else if (MLRFeedback == 1){
                resetColorFilters();
                manual_imageView_left.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

            } else if (MLRFeedback == 2){
                resetColorFilters();
                manual_imageView_right.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

            } else if (MUDFeedback == 1){
                resetColorFilters();
                manual_imageView_up.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

            } else if (MUDFeedback == 2){
                resetColorFilters();
                manual_imageView_down.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));
            }
        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }
    }

    public void setLineMotor(Boolean state) {
        try {
            JSONObject JSONOutput = new JSONObject();
            JSONOutput.put("command_ID", "PWDA");
            if(state == true) {
            /*Start the line*/
                JSONOutput.put("TMD", true);
                JSONOutput.put("PCS", 3);
            } else {
            /*Stop the line*/
                JSONOutput.put("TMD", false);
                JSONOutput.put("PCS", 1);
            }
            mFragmentInteraction.onSendCommand(JSONOutput + "\r\n");
        } catch(JSONException exc) {
            Log.d("JSON exception", exc.getMessage());
        }
    }

    private void resetColorFilters() {
        manual_imageView_forward.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_backward.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_up.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_down.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_left.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_right.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }
}
