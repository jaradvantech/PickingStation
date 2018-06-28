package com.example.administrator.PickingStation;

import android.content.Context;
import android.util.Log;

public class BrickManager {

    static Context localContext;

    static void initBrickManager(Context mContext) {
        localContext = mContext;
    }

    static String getGradeFromRaw(int type) {
        return localContext.getResources().getString(localContext.getResources().getIdentifier("brick_grade_" + (type >> 4), "string", localContext.getPackageName()));
    }

    static int getColorFromRaw(int type) {
        return localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + (type & 15), "color", localContext.getPackageName()));
    }

    static String getGrade(int grade) {
        return localContext.getResources().getStringArray(R.array.Grades)[grade];
    }

    static int getColor(int color) {
        return  localContext.getResources().getColor(localContext.getResources().getIdentifier("brick_color_" + color, "color", localContext.getPackageName()));
    }
}
