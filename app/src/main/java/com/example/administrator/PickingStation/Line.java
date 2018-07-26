package com.example.administrator.PickingStation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import static android.support.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_VERY_LOW;
import static com.example.administrator.PickingStation.BrickManager.getGradeFromRaw;
import static com.example.administrator.PickingStation.Commands.RPRV;


public class Line extends Fragment {
    private OnFragmentInteractionListener mListener;

    //JAGM: Arbitrary constants to control the position of the bricks on the line
    private float OriginPosition = -116.35f; //smaller=later
    private float EncoderToPixelDivider = 10.1f; //mas grande, lo suelta antes
    private ImageView PhysicalPallet[];
    private TextView PhysicalPallet_UID[];
    private TextView bricksOnTheLine[] = new TextView[12];
    private Button PhysicalPallet_TopBrick[];
    private boolean autoUpdate;
    private int armNumber;
    private int palletNumber;
    private int NumberOfBricksOnLine;
    private ArrayList DNIinUse;
    private int current_ManipulatorDNIs[];
    private int last_ManipulatorDNIs[];
    private int destinationPallets[];
    private int palletCoords[][];
    private final int RPRV_PERIOD = 300;
    private final int X = 0;
    private final int Y = 1;
    private final String NULL_UID = "0000000000000000";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private JSONArray palletInformation = null;
    private JSONArray bricksOnLine = null;
    private JSONArray takenBricks = null;

    private LayoutInflater inflater;
    private ViewGroup container;
    private LinearLayout holder;
    private ConstraintLayout main;
    private View view;


    public Line() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_line, container, false);
        holder = view.findViewById(R.id.line_linearLayout_holder);
        main = view.findViewById(R.id.line_constraintLayout_mainContainer);
        this.inflater = inflater;
        this.container = container;

        setNumberOfManipulators(SettingManager.getArms());

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLineFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onSendCommand(String command);
    }

    /*
     * Update all graphic information
     */
    public void updateLineBrickInfo(String message) {
        try {
            JSONObject JSONparser = new JSONObject(message);
            palletInformation = JSONparser.getJSONArray("palletInformation");
            bricksOnLine = JSONparser.getJSONArray("bricksOnTheLine");
            takenBricks = JSONparser.getJSONArray("bricksTakenByManipulators");

            int receivedPalletNumber = palletInformation.length(); //this won't be necessary in the future.
            if(receivedPalletNumber != palletNumber) {
                Log.e("Line", "Number of pallets has changed from " + palletNumber + " to " + receivedPalletNumber + "!!!!" );
                SettingManager.setArms(receivedPalletNumber/2);
                this.setNumberOfManipulators(receivedPalletNumber/2);
            }
            NumberOfBricksOnLine = bricksOnLine.length();

        } catch (Exception jsonExc) {
            Log.e("JSON Exception", "updateLineBrickInfo(): " + jsonExc.getMessage());
        }

        DNIinUse = new ArrayList<>();

        updateBricksInPallets();

        updateBricksOnLine();

        checkForPickedBricks();

        hideUnusedBricks();
    }

    /*
     *  Update UID and content of every pallet.
     */
    private void updateBricksInPallets() {
        //For every pallet in the line
        for (int i=0; i<palletNumber; i++) {
            String palletUID = NULL_UID;
            int numberOfBricks=0, topBrick=0;
            try {
                palletUID = palletInformation.getJSONObject(i).getString("palletUID");
                numberOfBricks = palletInformation.getJSONObject(i).getInt("numberOfBricks");
                topBrick = palletInformation.getJSONObject(i).getInt("topBrick");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", "updateBricksInPallets(): " + jsonExc.getMessage());
            }

            //Update pallets
            if (palletUID.contentEquals(NULL_UID)) {
                PhysicalPallet[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID[i].setText(palletUID);
                PhysicalPallet_TopBrick[i].setBackgroundColor(Color.TRANSPARENT);
                PhysicalPallet_TopBrick[i].setText("");
            } else {
                PhysicalPallet[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID[i].setText(palletUID);
                if (numberOfBricks > 0) {
                    try {
                        PhysicalPallet_TopBrick[i].setBackground(BrickManager.getBackgroundFromRaw(topBrick));
                        PhysicalPallet_TopBrick[i].setText("#: " + numberOfBricks + "\n\n" + getGradeFromRaw(topBrick));
                    } catch (Exception e) {
                        Log.e("exc", e.toString());
                    }
                } else {
                    PhysicalPallet_TopBrick[i].setBackgroundColor(Color.TRANSPARENT);
                    PhysicalPallet_TopBrick[i].setText("");
                }
            }
        }
    }

    /*
     * Update graphic information of the bricks laying in the conveyor line
     */
    private void updateBricksOnLine() {
        for (int i=0; i<NumberOfBricksOnLine; i++) {
            try {
                int brickPosition = bricksOnLine.getJSONObject(i).getInt("position");
                int brickRaw = bricksOnLine.getJSONObject(i).getInt("type");
                int assignedPallet = bricksOnLine.getJSONObject(i).getInt("assignedPallet");
                int brickDNI = bricksOnLine.getJSONObject(i).getInt("DNI");

                destinationPallets[brickDNI] = assignedPallet;

                //Make brick visible
                showBrick(brickDNI, brickRaw, brickPosition, assignedPallet);
                DNIinUse.add(brickDNI);

                //Update visual position of the brick
                SpringForce force = new SpringForce();
                force.setDampingRatio(DAMPING_RATIO_NO_BOUNCY).setStiffness(STIFFNESS_VERY_LOW);
                float finalpos = OriginPosition + brickPosition / (EncoderToPixelDivider);
                if(finalpos > palletCoords[X][assignedPallet]) {
                    finalpos = palletCoords[X][assignedPallet]; //Do not allow brick to go beyond its pallet.
                }
                final SpringAnimation springAnim = new SpringAnimation(bricksOnTheLine[brickDNI], DynamicAnimation.X).setSpring(force);
                springAnim.animateToFinalPosition(finalpos);

            } catch (Exception jsonExc) {
                Log.e("JSON Exception", "updateBricksOnLine(): " + jsonExc.getMessage());
            }
        }
    }

    private void checkForPickedBricks() {
        for (int j=0; j<armNumber; j++) {
            try {
                int currentXEncoderValue = takenBricks.getJSONObject(j).getInt("currentXEncoderValue"); //j-1 because index salad
                int ValueOfCatchDrop = takenBricks.getJSONObject(j).getInt("valueOfCatchDrop");
                int Position = takenBricks.getJSONObject(j).getInt("position");
                int rawType = takenBricks.getJSONObject(j).getInt("type");
                int assignedPallet = takenBricks.getJSONObject(j).getInt("assignedPallet");
                int DNI = takenBricks.getJSONObject(j).getInt("DNI");

                current_ManipulatorDNIs[j] = DNI;

                //If certain manipulator was empty before, and now it has something on it,
                //this means it has picked up something. Logic, isn't it?
                if ((current_ManipulatorDNIs[j] != last_ManipulatorDNIs[j]) && current_ManipulatorDNIs[j] != 0) {
                    Log.d("DNI", "has been taken");
                    moveToPalletAnim(current_ManipulatorDNIs[j], assignedPallet);
                    last_ManipulatorDNIs[j] = current_ManipulatorDNIs[j];
                }

                //If DNI picked by manipulator
                if(current_ManipulatorDNIs[j] != 0) {
                    DNIinUse.add(DNI);
                }

            } catch (Exception jsonExc) {
                Log.e("JSON Exception", "checkForPickedBricks(): " + jsonExc.getMessage());
            }
        }
    }

    /*
     * Use Object animator to move a certain brick to its pallet
     */
    private void moveToPalletAnim(int mBrickDNI, int destinationPallet) {
        float finalpos_X = palletCoords[0][destinationPallet];
        float finalpos_Y = palletCoords[1][destinationPallet];

        ObjectAnimator editorLayoutAnimation_x = ObjectAnimator.ofFloat(bricksOnTheLine[mBrickDNI], "x", finalpos_X);
        ObjectAnimator editorLayoutAnimation_y = ObjectAnimator.ofFloat(bricksOnTheLine[mBrickDNI], "y", finalpos_Y);
        AnimatorSet animSetline = new AnimatorSet();
        animSetline.playTogether(editorLayoutAnimation_x, editorLayoutAnimation_y);

        //Move for three seconds (or whatever it takes in real life)
        animSetline.setDuration(2500);

        //Wait for 1s (pick-up motion delay)
        animSetline.setStartDelay(1000);
        animSetline.start();
    }

    /*
     * Make certain brick visible and display information on top of it
     */
    private void showBrick(int mBrickDNI, int mBrickRaw, int mBrickPosition, int mAssignedPallet) {

        if(mBrickDNI == 0) {
            Log.e("ERROR", "Received DNI 0 from server");
            Toast.makeText(getActivity(), "Received DNI 0 from server", Toast.LENGTH_LONG).show();
        } else {
            //RBS TODO there is a better way to insert variables inside strings while supporting different languages
            bricksOnTheLine[mBrickDNI].setVisibility(View.VISIBLE);
            bricksOnTheLine[mBrickDNI].setText(getString(R.string.At) + " " + mBrickPosition + "\nDNI: " + mBrickDNI + "\n" + BrickManager.getGradeFromRaw(mBrickRaw) + "\n " + getString(R.string.To) + " " + mAssignedPallet);
            bricksOnTheLine[mBrickDNI].setBackground(BrickManager.getBackgroundFromRaw(mBrickRaw));
        }
    }

    private void hideUnusedBricks() {
        for (int i = 0; i< bricksOnTheLine.length; i++) {

            //If the brick is no longer in the line
            if (DNIinUse.contains(i) == false) {
                //Make invisible
                bricksOnTheLine[i].setVisibility(View.INVISIBLE);

                //Move to beginning of line
                ObjectAnimator hideAnimation_x = ObjectAnimator.ofFloat(bricksOnTheLine[i], "x", palletCoords[X][0]);
                ObjectAnimator hideAnimation_y = ObjectAnimator.ofFloat(bricksOnTheLine[i], "y", palletCoords[Y][0]);
                AnimatorSet animSetline = new AnimatorSet();
                animSetline.playTogether(hideAnimation_x, hideAnimation_y);
                animSetline.setDuration(10);
                animSetline.start();
            }
        }
    }


    /*****************************************************
     *                        --AUTOUPDATE CONTENTS--
     *****************************************************/
    final Runnable timer_lines = new Runnable() {
        @Override
        public void run() {
            mListener.onSendCommand(RPRV);
            if(autoUpdate == false) handler.removeCallbacksAndMessages(null);
            else handler.postDelayed(this, RPRV_PERIOD);
        }
    };

    public void whenEnteringFragment() {
        handler.postDelayed(timer_lines, RPRV_PERIOD);
        autoUpdate=true;
    }

    public void whenLeavingFragment() {
        autoUpdate = false;
    }



    /*****************************************************
     *               --DRAW SCREEN FOR N MANIPULATORS--
     *****************************************************/
    public void setNumberOfManipulators(int n) {
        this.armNumber = n;
        this.palletNumber = 2*n;

        //Draw screen
        holder.removeAllViews();
        View currentViews[] = new View[armNumber];

        holder.addView(inflater.inflate(R.layout.linesegment_start, container, false));
        currentViews[0] = inflater.inflate(R.layout.linesegment_first, container, false);
        holder.addView(currentViews[0]);
        for(int i=1; i<armNumber; i++) {
            currentViews[i] = inflater.inflate(R.layout.linesegment_middle, container, false);
            holder.addView(currentViews[i]);
        }
        holder.addView(inflater.inflate(R.layout.linesegment_end, container, false));

        current_ManipulatorDNIs = new int[armNumber];
        last_ManipulatorDNIs = new int[armNumber];
        destinationPallets = new int[palletNumber];
        PhysicalPallet = new ImageView[palletNumber];
        PhysicalPallet_UID = new TextView[palletNumber];
        PhysicalPallet_TopBrick = new Button[palletNumber];

        //Find Elements
        int currentPallet = 0;
        for(int i=0; i<armNumber; i++) {
            PhysicalPallet_TopBrick[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_button_palletDown);
            PhysicalPallet_UID[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_textView_UIDDown);
            PhysicalPallet[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_imageView_palletDown);
            currentPallet++;

            PhysicalPallet_TopBrick[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_button_palletUp);
            PhysicalPallet_UID[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_textView_UIDup);
            PhysicalPallet[currentPallet] = currentViews[i].findViewById(R.id.lineSegment_imageView_palletUp);
            currentPallet++;
        }

        //Set listeners for buttons
        for(int i=0; i<palletNumber; i++) {
            final int fi = i;
            PhysicalPallet_TopBrick[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Ask to server, so when switching layouts is done, information is already there.
                    try {
                        JSONObject RGMVCommand = new JSONObject();
                        RGMVCommand.put("command_ID", "RGMV");
                        RGMVCommand.put("palletNumber", fi+1); //pallets start at 1 serverside
                        mListener.onSendCommand(RGMVCommand.toString());
                    } catch(JSONException exc) {
                        Log.d("JSON exception", exc.getMessage());
                    }
                    ((MainActivity)getActivity()).switchToLayout(R.id.nav_editor);
                }
            });
        }

        //Create the bricks that will be shown on the line //TODO RBS make fully dynamic
        for (int i=0; i<bricksOnTheLine.length; i++) {
            bricksOnTheLine[i] = new TextView(getActivity());
            bricksOnTheLine[i].setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(94, 94);
            bricksOnTheLine[i].setLayoutParams(params);
            bricksOnTheLine[i].setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            bricksOnTheLine[i].setVisibility(View.VISIBLE);
            main.addView(bricksOnTheLine[i]);
        }

        //Generate pallet coords;
        //index 0 is start point before the line. indexes 1....n are the pallets.
        //Y is either 408 (down pallet) or 68 (top pallet)
        //X starts at pos 143, and then every pallet is 200dp wide
        palletCoords = new int[2][palletNumber];

        palletCoords[X][0] = -100;
        palletCoords[Y][0] = 230;
        int manipulatorN = -1;
        for (int i=1; i<palletNumber; i++) {
            //Y: even pallets are at top, odd pallets at bottom
            if(i%2 == 0) {
                palletCoords[Y][i] = 68;
            } else {
                palletCoords[Y][i] = 408;
                manipulatorN++;
            }
            //X;
            palletCoords[X][i] = 143 + 200*manipulatorN;
        }
    }
}
