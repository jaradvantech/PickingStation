package com.example.administrator.PickingStation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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

import java.util.ArrayList;

import static android.support.animation.SpringForce.DAMPING_RATIO_NO_BOUNCY;
import static android.support.animation.SpringForce.STIFFNESS_VERY_LOW;

//Esta clase es una chapuza, de principio a fin

public class Line extends Fragment {
    private OnFragmentInteractionListener mListener;

    //JAGM: Arbitrary constants to control the position of the bricks on the line
    private float OriginPosition = -116.35f; //smaller=later
    private float EncoderToPixelDivider = 10.1f; //mas grande, lo suelta antes

    private final ImageView PhysicalPallet[] = new ImageView[10 + 1];
    private final TextView PhysicalPallet_UID_Viewer[] = new TextView[10 + 1];
    private final TextView PhysicalBricksOnTheLine_Brick_Viewer[] = new TextView[12 + 1];
    private final Button PhysicalPallet_TopBrick[] = new Button[10 + 1];

    private int armNumber;
    private View view;
    private int NumberOfPallets;
    private int NumberOfBricksOnLine;
    private ArrayList DNIinUse = new ArrayList<>();
    private int current_ManipulatorDNIs[] = new int[5+1];
    private int last_ManipulatorDNIs[] = new int[5+1];
    private int destinationPallets[] = new int[10+1];

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
    }

    /*
     * Update all graphic information
     */
    public void updateLineBrickInfo(String message) {

        NumberOfPallets = Integer.parseInt(message.substring(5, 7));
        NumberOfBricksOnLine = Integer.parseInt(message.substring(27 + (NumberOfPallets - 1) * 19, 29 + (NumberOfPallets - 1) * 19));

        DNIinUse = new ArrayList<>();

        updateBricksInPallets(message);

        updateBricksOnLine(message);

        checkForPickedBricks(message);

        hideUnusedBricks();
    }

    /*
     *  Update UID and content of every pallet. Argument is the response
     *  of an RPRV command.
     */
    private void updateBricksInPallets(String message) {

        //For every pallet in the line
        for (int i = 1; i <= NumberOfPallets; i++) {
            String cutedUID = message.substring(8 + (i - 1) * 19, 24 + (i - 1) * 19);
            int cutedNumberOfBricks = message.charAt(24 + (i - 1) * 19) - 17;
            int cutedTopBrick = message.charAt(25 + (i - 1) * 19);

            //Update pallets
            if (cutedUID.contentEquals("0000000000000000")) {
                PhysicalPallet[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID_Viewer[i].setVisibility(View.INVISIBLE);
                PhysicalPallet_UID_Viewer[i].setText(cutedUID);
                PhysicalPallet_TopBrick[i].setBackgroundColor(Color.TRANSPARENT);
                PhysicalPallet_TopBrick[i].setText("");
            } else {
                PhysicalPallet[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID_Viewer[i].setVisibility(View.VISIBLE);
                PhysicalPallet_UID_Viewer[i].setText(cutedUID);
                if (cutedNumberOfBricks > 0) {
                    //Log.d("MemoryValue", "Memory uid: " + cutedUID );
                    //Log.d("MemoryValue", "Memory raw nob: " + cutedNumberOfBricks );
                    //Log.d("MemoryValue", "Memory raw top: " + cutedTopBrick );
                    int colorID = getResources().getIdentifier("brick_color_" + (cutedTopBrick & 15), "color", getContext().getPackageName());
                    String grade = getResources().getString(getResources().getIdentifier("brick_grade_" + (cutedTopBrick >> 4), "string", getContext().getPackageName()));

                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(getResources().getColor(colorID)); // Changes this drawable to use a single color instead of a gradient
                    gd.setCornerRadius(1);
                    gd.setStroke(2, 0xFF000000);
                    PhysicalPallet_TopBrick[i].setBackground(gd);

                    PhysicalPallet_TopBrick[i].setText("#: " + cutedNumberOfBricks + "\n\n" + grade);
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
    private void updateBricksOnLine(String message) {

        //For every brick
        for (int i = 1; i <= NumberOfBricksOnLine; i++) {
            int brickPosition = Integer.parseInt(message.substring(30 + (NumberOfPallets - 1) * 19 + (i - 1) * 12, 36 + (NumberOfPallets - 1) * 19 + (i - 1) * 12));
            int brickRaw = message.charAt(36 + (NumberOfPallets - 1) * 19 + (i - 1) * 12);
            int assignedPallet = Integer.parseInt(message.substring(37 + (NumberOfPallets - 1) * 19 + (i - 1) * 12, 39 + (NumberOfPallets - 1) * 19 + (i - 1) * 12));
            int brickDNI = Integer.parseInt(message.substring(39 + (NumberOfPallets - 1) * 19 + (i - 1) * 12, 41 + (NumberOfPallets - 1) * 19 + (i - 1) * 12));

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

    private void checkForPickedBricks(String message) {

        //Skip everything until the part of the command where the manipulator info is
        int paddingOfLastBlock = 29 + (NumberOfPallets - 1) * 19 + (NumberOfBricksOnLine * 12);
        //Log.d("despues", String.valueOf(message.substring(paddingOfLastBlock)));

        try {
            for (int j = 1; j<6; j++) {
                int ActualValueEncoder = Integer.parseInt(message.substring(1 + paddingOfLastBlock, 7 + paddingOfLastBlock));
                int ValueOfCatchDrop = Integer.parseInt(message.substring(7 + paddingOfLastBlock, 13 + paddingOfLastBlock));
                int Position = Integer.parseInt(message.substring(13 + paddingOfLastBlock, 19 + paddingOfLastBlock));
                int rawType = Character.getNumericValue(message.charAt(19 + paddingOfLastBlock));
                int assignedPallet = Integer.parseInt(message.substring(20 + paddingOfLastBlock, 22 + paddingOfLastBlock));
                int DNI = Integer.parseInt(message.substring(22 + paddingOfLastBlock, 24 + paddingOfLastBlock));

                current_ManipulatorDNIs[j] = Integer.parseInt(message.substring(22 + paddingOfLastBlock, 24 + paddingOfLastBlock));

                //If certain manipulator was empty before, and now it has something on it,
                //this means it's picking up something. Logic, isnt it?
                if ((current_ManipulatorDNIs[j] != last_ManipulatorDNIs[j]) && current_ManipulatorDNIs[j] != 0) {
                    Log.d("DNI", "has changed");
                    moveToPalletAnim(current_ManipulatorDNIs[j], assignedPallet);
                    last_ManipulatorDNIs[j] = current_ManipulatorDNIs[j];
                }

                //If DNI picked by manipulator
                if(current_ManipulatorDNIs[j] != 0) {
                    DNIinUse.add(DNI);
                }

                //Skip to next manipulator
                paddingOfLastBlock += 24;
            }
        } catch (NumberFormatException formatException) {
            Log.d("BAD CMD", formatException.toString());
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


    private void setNumberOfManipulators(int n) {
        this.armNumber = n;

        LinearLayout mainLayout = (LinearLayout) view.findViewById(R.id.line_linearLayout_canvas);

        /*Structure is as follows*/
        //Line Start --> always
        //first_pallet --> always (n>1)
        //middle_pallet --> As required (n-1) times
        // ....
        //Line end


    }
}
