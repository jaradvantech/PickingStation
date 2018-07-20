package com.example.administrator.PickingStation;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class AlarmObject {

    private String type;
    private String description;
    private String time;
    private String source;


    AlarmObject(){
    }

    AlarmObject(String mType, String mTime, String mSource, String mDescription){
        this.type = mType;
        this.time = mTime;
        this.source = mSource;
        this.description = mDescription;
    }

    public String toString(){
        return "Alarm tipe: " + type + ", Raised at " + time + ", source: " + source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    /* RBS
     *  Implement comparator class so alarms can be sorted by timestamp
     *  Yeah, shit is getting serious with Java now 8)
     */
    class SortByDate implements Comparator<AlarmObject> {

        public int compare(AlarmObject a, AlarmObject b)
        {
            int result = 0;
            Date timestampA, timestampB;
            try {
                timestampA = new SimpleDateFormat("HH:mm:ss dd-MM-yy ").parse(a.getTime());
                timestampB = new SimpleDateFormat("HH:mm:ss dd-MM-yy ").parse(b.getTime());
                result = timestampA.compareTo(timestampB);

            } catch (ParseException parserException) {
                Log.e("AlarmObject", "Invalid Timestamp");
            }
            return result;
        }
    }
}
