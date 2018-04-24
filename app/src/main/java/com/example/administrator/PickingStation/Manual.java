package com.example.administrator.PickingStation;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

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
    private ImageView manual_imageView_question_top;

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
        manual_imageView_question_top = (ImageView) view.findViewById(R.id.manual_imageView_question_top);

        final GifImageView manual_GifimageView_sucker_glow = (GifImageView) view.findViewById(R.id.manual_GifimageView_sucker_glow);
        final GifImageView manual_GifimageView_motor_glow = (GifImageView) view.findViewById(R.id.manual_GifimageView_motor_glow);
        final GifImageView manual_GifimageView_arrows_glow = (GifImageView) view.findViewById(R.id.manual_GifimageView_arrows_glow);

        final GifImageView manual_GifimageView_sucker_animation = (GifImageView) view.findViewById(R.id.manual_GifimageView_sucker_animation);
        final GifImageView manual_GifimageView_line_wheel = (GifImageView) view.findViewById(R.id.manual_GifimageView_line_wheel);

        final GifDrawable manual_GifimageView_sucker_glow_Drawable = (GifDrawable)manual_GifimageView_sucker_glow.getBackground();
        final GifDrawable manual_GifimageView_motor_glow_Drawable = (GifDrawable)manual_GifimageView_motor_glow.getBackground();
        final GifDrawable manual_GifimageView_arrows_glow_Drawable = (GifDrawable)manual_GifimageView_arrows_glow.getBackground();

        final GifDrawable manual_GifimageView_sucker_animation_Drawable = (GifDrawable)manual_GifimageView_sucker_animation.getBackground();
        final GifDrawable manual_GifimageView_line_wheel_Drawable = (GifDrawable)manual_GifimageView_line_wheel.getBackground();

        final TextView currentArmText = (TextView) view.findViewById(R.id.debug_currentArm);
        final TextView manual_textView_line = (TextView) view.findViewById(R.id.manual_textView_line);


        manual_GifimageView_sucker_glow_Drawable.stop();
        manual_GifimageView_sucker_glow_Drawable.seekToFrame(0);
        manual_GifimageView_motor_glow_Drawable.stop();
        manual_GifimageView_motor_glow_Drawable.seekToFrame(0);
        manual_GifimageView_arrows_glow_Drawable.stop();
        manual_GifimageView_arrows_glow_Drawable.seekToFrame(0);

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
                    //manual_GifimageView_line_wheel_Drawable.seekToFrame(0);
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

        manual_imageView_question_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                manual_GifimageView_sucker_glow.setVisibility(View.VISIBLE);
                manual_GifimageView_motor_glow.setVisibility(View.VISIBLE);
                manual_GifimageView_arrows_glow.setVisibility(View.VISIBLE);

                if(manual_GifimageView_sucker_glow_Drawable.isPlaying()){
                    manual_GifimageView_sucker_glow_Drawable.stop();
                    manual_GifimageView_sucker_glow_Drawable.seekToFrame(0);

                    manual_GifimageView_motor_glow_Drawable.stop();
                    manual_GifimageView_motor_glow_Drawable.seekToFrame(0);

                    manual_GifimageView_arrows_glow_Drawable.stop();
                    manual_GifimageView_arrows_glow_Drawable.seekToFrame(0);
                    manual_GifimageView_arrows_glow.setVisibility(View.INVISIBLE);
                }
                else{
                    manual_GifimageView_sucker_glow_Drawable.reset();
                    manual_GifimageView_motor_glow_Drawable.reset();
                    manual_GifimageView_arrows_glow_Drawable.reset();
                }

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
        String writeCommand = "PWDA_14_";

        writeCommand += String.format("%02d", mArm);
        writeCommand += "_";
        writeCommand += "X";  //SBD
        writeCommand += "X"; //MR
        writeCommand += "X"; //SBFA
        writeCommand += "X"; //SBFB
        writeCommand += "X"; //BCRSA
        writeCommand += "X"; //BCRSB
        writeCommand += boolToString(!manualMode[mArm]);
        writeCommand += boolToString(VV[mArm]);
        writeCommand += "_";
        writeCommand += String.format("%03d", MFB);
        writeCommand += "_";
        writeCommand += String.format("%03d", MLR);
        writeCommand += "_";
        writeCommand +=  String.format("%03d", MUD);
        writeCommand += "_";
        writeCommand += "XXX"; //COD
        writeCommand += "_";
        writeCommand += "XXX"; //WTDWT
        writeCommand += "_";
        writeCommand += "XXXXXX"; //PZA
        writeCommand += "_";
        writeCommand += "XXXXXX"; //VOCD
        writeCommand += "_";
        writeCommand += "X"; //CE
        writeCommand += "X"; //TP
        writeCommand += "X"; //ITT
        writeCommand += "X"; //TMD
        writeCommand += "_";
        writeCommand += "XXX"; //PCS
        writeCommand += "_";
        writeCommand += "XXXXXX"; //ADD
        writeCommand += "_";
        writeCommand += "XXXXXX"; //ZASV
        writeCommand += "_";
        writeCommand += "XXXXXX"; //TPOX_AGB
        writeCommand += "_";
        writeCommand += "XXXXXX"; //TPOX_AAD
        writeCommand += "\r\n";

        mFragmentInteraction.onSendCommand(writeCommand);
    }

    void serverResponse(String mResponse, Context activityContext){
        //Tint icons as feedback
        if(mResponse.charAt(22) == '1') {
            resetColorFilters();
            manual_imageView_forward.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

        } else if (mResponse.charAt(22) == '2'){
            resetColorFilters();
            manual_imageView_backward.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

        } else if (mResponse.charAt(26) == '1'){
            resetColorFilters();
            manual_imageView_left.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

        } else if (mResponse.charAt(26) == '2'){
            resetColorFilters();
            manual_imageView_right.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

        } else if (mResponse.charAt(30) == '1'){
            resetColorFilters();
            manual_imageView_up.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));

        } else if (mResponse.charAt(30) == '2'){
            resetColorFilters();
            manual_imageView_down.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 0,0)));
        }
    }

    //RBS quick&dirty I need it working now. Merge with the other function in the future.
    //sorry :(
    public void setLineMotor(Boolean state) {
        String writeCommand = "PWDA_14_";

        writeCommand += "01"; //arm number could be whatevs
        writeCommand += "_";
        writeCommand += "X";  //SBD
        writeCommand += "X"; //MR
        writeCommand += "X"; //SBFA
        writeCommand += "X"; //SBFB
        writeCommand += "X"; //BCRSA
        writeCommand += "X"; //BCRSB
        writeCommand += "X";
        writeCommand += "X";
        writeCommand += "_";
        writeCommand += "XXX"; //COD
        writeCommand += "_";
        writeCommand += "XXX"; //COD
        writeCommand += "_";
        writeCommand +=  "XXX"; //COD
        writeCommand += "_";
        writeCommand += "XXX"; //COD
        writeCommand += "_";
        writeCommand += "XXX"; //WTDWT
        writeCommand += "_";
        writeCommand += "XXXXXX"; //PZA
        writeCommand += "_";
        writeCommand += "XXXXXX"; //VOCD
        writeCommand += "_";
        writeCommand += "X"; //CE
        writeCommand += "X"; //TP
        writeCommand += "X"; //ITT

        if(state == true) {
            /*Start the line*/
            writeCommand += "1"; //TMD       //SET THIS TO TRUE
            writeCommand += "_";
            writeCommand += "003"; //PCS  //AND PC STATE TO 3

        } else {
            /*Stop the line*/
            writeCommand += "0"; //TMD       //SET THIS TO FALSE
            writeCommand += "_";
            writeCommand += "001"; //PCS  //AND PC STATE TO 1
        }

        writeCommand += "_";
        writeCommand += "XXXXXX"; //ADD
        writeCommand += "_";
        writeCommand += "XXXXXX"; //ZASV
        writeCommand += "_";
        writeCommand += "XXXXXX"; //TPOX_AGB
        writeCommand += "_";
        writeCommand += "XXXXXX"; //TPOX_AAD
        writeCommand += "\r\n";

        mFragmentInteraction.onSendCommand(writeCommand);
    }

    private void resetColorFilters() {
        manual_imageView_forward.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_backward.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_up.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_down.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_left.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
        manual_imageView_right.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorSecondaryPale)));
    }

    private String boolToString(Boolean mBol) {
        if (mBol)
            return "1";
        else
            return "0";
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand( String command );
    }
}
