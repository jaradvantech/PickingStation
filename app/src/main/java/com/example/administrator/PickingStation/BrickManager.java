package com.example.administrator.PickingStation;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;

public class BrickManager {

    static Context localContext;

    static void initBrickManager(Context mContext) {
        localContext = mContext;
    }

    static String getGradeFromRaw(int type) {
        return localContext.getResources().getString(localContext.getResources().getIdentifier("brick_grade_" + ((type >> 4)-1), "string", localContext.getPackageName()));
    }

    static int getColorFromRaw(int type) {
        return localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + ((type & 15)-1), "color", localContext.getPackageName()));
    }

    //In the future we will use this to generate solid colors or images as backgrounds
    static GradientDrawable getBackgroundFromRaw(int type) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(getColorFromRaw(type)); // Changes this drawable to use a single color instead of a gradient
        gd.setCornerRadius(1);
        gd.setStroke(2, 0xFF000000);
        return gd;
    }

    static GradientDrawable getBackgroundFromColor(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + color, "color", localContext.getPackageName()))); // Changes this drawable to use a single color instead of a gradient
        gd.setCornerRadius(1);
        gd.setStroke(2, 0xFF000000);
        return gd;
    }

    static String getGrade(int grade) {
        return localContext.getResources().getStringArray(R.array.Grades)[grade];
    }

    static String getColorName(int index) {
        return localContext.getResources().getStringArray(R.array.Colours)[index];
    }

    static int getTotalColors() {
        return 16;
    }

    static int getTotalGrades() {
        return 7;
    }
}
