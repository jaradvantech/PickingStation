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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.support.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_VERY_LOW;
import static com.example.administrator.PickingStation.Commands.RPRV;

//Pese a mis esfuerzos de adecentarla, esta clase es una chapuza, de principio a fin. firmado RBS

public class Line extends Fragment {
    private OnFragmentInteractionListener mListener;

    //JAGM: Arbitrary constants to control the position of the bricks on the line
    private float OriginPosition = -116.35f; //smaller=later
    private float EncoderToPixelDivider = 10.1f; //mas grande, lo suelta antes
    private final ImageView PhysicalPallet[] = new ImageView[10 + 1];
    private final TextView PhysicalPallet_UID_Viewer[] = new TextView[10 + 1];
    private final TextView PhysicalBricksOnTheLine_Brick_Viewer[] = new TextView[12 + 1];
    private final Button PhysicalPallet_TopBrick[] = new Button[10 + 1];
    private boolean autoUpdate;
    private int armNumber;
    private View view;
    private int MANIPULATORS = 5; //Will be variable in the future. default 5
    private int NumberOfPallets;
    private int NumberOfBricksOnLine;
    private ArrayList DNIinUse = new ArrayList<>();
    private int current_ManipulatorDNIs[] = new int[MANIPULATORS+1];
    private int last_ManipulatorDNIs[] = new int[MANIPULATORS+1];
    private int destinationPallets[] = new int[(2*MANIPULATORS)+1];
    private final int RPRV_PERIOD = 300;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private JSONArray palletInformation = null;
    private JSONArray bricksOnLine = null;
    private JSONArray takenBricks = null;

   /* Convert from pallet number (1-10) to px. (index 0 means origin)
    * (pixel) palletCoords[(x=0, y=1)][(pallet num)]
    */
    private final int palletCoords[][]={
        {-100, 143, 143, 343, 343, 543, 543, 743, 743, 943, 943},
        {  230, 408,  68, 408,   68, 408,   68, 408,  68,  408,  68}
    };

    public Line() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Set layout
        view = inflater.inflate(R.layout.fragment_line, container, false);

        //Init bricks on the line
        for (int i = 1; i < PhysicalBricksOnTheLine_Brick_Viewer.length; i++) {
            String PhysicalBricksOnTheLine_Brick_ViewerID = "line_textView_brickOnTheLine_" + (i);

            int PhysicalBricksOnTheLine_Brick_ViewerIDresID = getResources().getIdentifier(PhysicalBricksOnTheLine_Brick_ViewerID, "id", getActivity().getPackageName());

            PhysicalBricksOnTheLine_Brick_Viewer[i] = ((TextView) view.findViewById(PhysicalBricksOnTheLine_Brick_ViewerIDresID));
        }


        for (int i = 1; i < PhysicalPallet.length; i++) {
            String PhysicalPalletID = "line_image_pallet_" + (i);
            String PhysicalPallet_UID_ViewerID = "line_textView_UID_" + (i);
            String PhysicalPallet_TopBrickID = "line_button_pallet_" + (i);

            int PhysicalPalletresID = getResources().getIdentifier(PhysicalPalletID, "id", getActivity().getPackageName());
            int PhysicalPallet_UID_ViewerIDresID = getResources().getIdentifier(PhysicalPallet_UID_ViewerID, "id", getActivity().getPackageName());
            int PhysicalPallet_TopBrickresID = getResources().getIdentifier(PhysicalPallet_TopBrickID, "id", getActivity().getPackageName());

            PhysicalPallet[i] = ((ImageView) view.findViewById(PhysicalPalletresID));
            PhysicalPallet_UID_Viewer[i] = ((TextView) view.findViewById(PhysicalPallet_UID_ViewerIDresID));
            PhysicalPallet_TopBrick[i] = ((Button) view.findViewById(PhysicalPallet_TopBrickresID));

            PhysicalPallet_TopBrick[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int index = 1;
                    for (int j = 1; j < PhysicalPallet_TopBrick.length; j++) {
                        if (PhysicalPallet_TopBrick[j].getId() == v.getId()) {
                            index = j;
                        }
                    }
                    mListener.ChangeToEditor(index);
                    //CODE MESSAGE HERE
                    //String mSend;
                    //mListener.onSendCommand(mSend);
                }
            });
        }

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
        void ChangeToEditor(int index);
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

            NumberOfPallets = palletInformation.length(); //this won't be necessary in the future.
            NumberOfBricksOnLine = bricksOnLine.length();

        } catch (Exception jsonExc) {
            Log.e("JSON Exception", jsonExc.getMessage());
        }

        DNIinUse = new ArrayList<>();

        updateBricksInPallets();

        updateBricksOnLine();

        checkForPickedBricks();

        hideUnusedBricks();
    }

    /*
     *  Update UID and content of every pallet. Argument is the response
     *  of an RPRV command.
     */
    private void updateBricksInPallets() {
        //For every pallet in the line
        for (int i = 1; i <= NumberOfPallets; i++) {
            String palletUID = "0000000000000000";
            int numberOfBricks=0, topBrick=0;
            try {
                palletUID = palletInformation.getJSONObject(i).getString("palletUID");
                numberOfBricks = palletInformation.getJSONObject(i).getInt("numberOfBricks");
                topBrick = palletInformation.getJSONObject(i).getInt("topBrick");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }

            //Update pallets
            if (palletUID.contentEquals("0000000000000000")) {
                PhysicalPallet[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID_Viewer[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID_Viewer[i].setText(palletUID);
                PhysicalPallet_TopBrick[i].setBackgroundColor(Color.TRANSPARENT);
                PhysicalPallet_TopBrick[i].setText("");
            } else {
                PhysicalPallet[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID_Viewer[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID_Viewer[i].setText(palletUID);
                if (numberOfBricks > 0) {
                    //Log.d("MemoryValue", "Memory uid: " + palletUID );
                    //Log.d("MemoryValue", "Memory raw nob: " + numberOfBricks );
                    //Log.d("MemoryValue", "Memory raw top: " + topBrick );
                    int colorID = getResources().getIdentifier("brick_color_" + (topBrick & 15), "color", getContext().getPackageName());
                    String grade = getResources().getString(getResources().getIdentifier("brick_grade_" + (topBrick >> 4), "string", getContext().getPackageName()));

                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(getResources().getColor(colorID)); // Changes this drawable to use a single color instead of a gradient
                    gd.setCornerRadius(1);
                    gd.setStroke(2, 0xFF000000);
                    PhysicalPallet_TopBrick[i].setBackground(gd);

                    PhysicalPallet_TopBrick[i].setText("#: " + numberOfBricks + "\n\n" + grade);
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
        for (int i = 1; i <= NumberOfBricksOnLine; i++) {
            int brickPosition=0, brickRaw=0, assignedPallet=0, brickDNI=0;
            try {
                brickPosition = bricksOnLine.getJSONObject(i).getInt("position");
                brickRaw = bricksOnLine.getJSONObject(i).getInt("type");
                assignedPallet = bricksOnLine.getJSONObject(i).getInt("assignedPallet");
                brickDNI = bricksOnLine.getJSONObject(i).getInt("DNI");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }

            destinationPallets[brickDNI] = assignedPallet;

            //Make brick visible
            showBrick(brickDNI, brickRaw, brickPosition, assignedPallet);
            DNIinUse.add(brickDNI);

            //Update visual position of the brick
            SpringForce force = new SpringForce();
            force.setDampingRatio(DAMPING_RATIO_NO_BOUNCY).setStiffness(STIFFNESS_VERY_LOW);

            //Calculate new position
            float finalpos = OriginPosition + brickPosition / (EncoderToPixelDivider);
            final SpringAnimation springAnim = new SpringAnimation(PhysicalBricksOnTheLine_Brick_Viewer[brickDNI], DynamicAnimation.X).setSpring(force);

            //Start animation
            springAnim.animateToFinalPosition(finalpos);
        }
    }

    private void checkForPickedBricks() {
        for (int j = 1; j<MANIPULATORS+1; j++) {
            int currentXEncoderValue=0, ValueOfCatchDrop=0, Position=0, rawType=0, assignedPallet=0, DNI=0;
            try {
                currentXEncoderValue = takenBricks.getJSONObject(j).getInt("currentXEncoderValue");
                ValueOfCatchDrop = takenBricks.getJSONObject(j).getInt("valueOfCatchDrop");
                Position = takenBricks.getJSONObject(j).getInt("position");
                rawType = takenBricks.getJSONObject(j).getInt("rawType");
                assignedPallet = takenBricks.getJSONObject(j).getInt("assignedPallet");
                DNI = takenBricks.getJSONObject(j).getInt("DNI");
            } catch (Exception jsonExc) {
                Log.e("JSON Exception", jsonExc.getMessage());
            }

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
        }
    }

    /*
     * Use Object animator to move a certain brick to its pallet
     */
    private void moveToPalletAnim(int mBrickDNI, int destinationPallet) {
        float finalpos_X = palletCoords[0][destinationPallet];
        float finalpos_Y = palletCoords[1][destinationPallet];

        ObjectAnimator editorLayoutAnimation_x = ObjectAnimator.ofFloat(PhysicalBricksOnTheLine_Brick_Viewer[mBrickDNI], "x", finalpos_X);
        ObjectAnimator editorLayoutAnimation_y = ObjectAnimator.ofFloat(PhysicalBricksOnTheLine_Brick_Viewer[mBrickDNI], "y", finalpos_Y);
        AnimatorSet animSetline = new AnimatorSet();
        animSetline.playTogether(editorLayoutAnimation_x, editorLayoutAnimation_y);

        //Move for three seconds (or whatever takes in real life)
        animSetline.setDuration(2500);

        //Wait for 1s (pick-up motion delay)
        animSetline.setStartDelay(1000);
        animSetline.start();
    }

    /*
     * Make certain brick visible and display information on top of it
     */
    private void showBrick(int mBrickDNI, int mBrickRaw, int mBrickPosition, int mAssignedPallet) {
        int colorID = getResources().getIdentifier("brick_color_" + (mBrickRaw & 15), "color", getContext().getPackageName());
        String grade = getResources().getString(getResources().getIdentifier("brick_grade_" + (mBrickRaw >> 4), "string", getContext().getPackageName()));

        if(mBrickDNI == 0) {
            Log.e("ERROR", "Received DNI 0 from server");
            Toast.makeText(getActivity(), "WARNING, DNI=0", Toast.LENGTH_LONG).show();

        } else {
            PhysicalBricksOnTheLine_Brick_Viewer[mBrickDNI].setVisibility(View.VISIBLE);
            PhysicalBricksOnTheLine_Brick_Viewer[mBrickDNI].setText(getString(R.string.At) + " " + mBrickPosition + "\nDNI: " + mBrickDNI + "\n" + grade + "\n " + getString(R.string.To) + " " + mAssignedPallet);

            //update visual value of brick colour
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(getResources().getColor(colorID)); // Changes this drawable to use a single color instead of a gradient
            gd.setCornerRadius(1);
            gd.setStroke(2, 0xFF000000);
            PhysicalBricksOnTheLine_Brick_Viewer[mBrickDNI].setBackground(gd);
        }
    }

    private void hideUnusedBricks() {
        for (int i = 1; i<PhysicalBricksOnTheLine_Brick_Viewer.length; i++) {

            //If the brick is no longer in the line
            if (DNIinUse.contains(i) == false) {

                //Make invisible
                PhysicalBricksOnTheLine_Brick_Viewer[i].setVisibility(View.INVISIBLE);

                //Move to beginning of line
                ObjectAnimator hideAnimation_x = ObjectAnimator.ofFloat(PhysicalBricksOnTheLine_Brick_Viewer[i], "x", palletCoords[0][0]);
                ObjectAnimator hideAnimation_y = ObjectAnimator.ofFloat(PhysicalBricksOnTheLine_Brick_Viewer[i], "y", palletCoords[1][0]);
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
            mListener.onSendCommand(RPRV); //Ask for the UIDs
            if(autoUpdate == false) handler.removeCallbacksAndMessages(null);
            else handler.postDelayed(this, RPRV_PERIOD);
        }
    };

    public void startUpdating() {
        handler.postDelayed(timer_lines, RPRV_PERIOD);
        autoUpdate=true;
    }

    public void stopUpdating() {
        autoUpdate = false;
    }




    /*****************************************************
     *               --DRAW SCREEN FOR N MANIPULATORS--
     *****************************************************/
    private void setNumberOfManipulators(int n) {
        this.armNumber = n;

        //LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.line_linearLayout_canvas);

        /*Structure is as follows*/
        //Line Start --> always
        //first_pallet --> always (n>1)
        //middle_pallet --> As required (n-1) times
        // ....
        //Line end


    }
}
