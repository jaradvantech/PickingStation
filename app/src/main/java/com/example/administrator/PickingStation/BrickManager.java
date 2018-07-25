package com.example.administrator.PickingStation;

import android.content.Context;
import android.util.Log;

public class BrickManager {

    static Context localContext;

    static void initBrickManager(Context mContext) {
        localContext = mContext;
    }

    //Raw brick already has the "+1" hardcoded into it
    static String getGradeFromRaw(int type) {
        return localContext.getResources().getString(localContext.getResources().getIdentifier("brick_grade_" + (type >> 4), "string", localContext.getPackageName()));
    }

    static int getColorFromRaw(int type) {
        return localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + (type & 15), "color", localContext.getPackageName()));
    }

    //starting at 0
    static String getGrade(int grade) {
        return localContext.getResources().getStringArray(R.array.Grades)[grade];
    }

    static String getColorName(int index) {
        return localContext.getResources().getStringArray(R.array.Colours)[index];
    }

    //before starting at 1, now starting at 0
    static int getColor(int color) {
        return  localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + (color+1), "color", localContext.getPackageName()));
    }

    //Grade = 0...
    //Color = 0...
    static int getRaw(int grade, int color) {
        return ((grade+1)<<4) + (color+1);
    }

    static int getTotalColors() {
        return 16;
    }

    static int getTotalGrades() {
        return 7;
    }
}
