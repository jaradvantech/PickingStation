package com.example.administrator.PickingStation;

public class LogListObject implements Comparable<LogListObject> {

    private String characteristic;
    private int totalBricks;
    private int NumberWithThisCharacteristic;
    private int percent;


    LogListObject(){
    }

    public LogListObject(String characteristic, int totalBricks, int numberWithThisCharacteristic) {
        this.characteristic = characteristic;
        this.totalBricks = totalBricks;
        this.NumberWithThisCharacteristic = numberWithThisCharacteristic;
        this.percent = this.NumberWithThisCharacteristic*100 / this.totalBricks ;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public int getTotalBricks() {
        return totalBricks;
    }

    public void setTotalBricks(int totalBricks) {
        this.totalBricks = totalBricks;
    }

    public int getNumberWithThisCharacteristic() {
        return NumberWithThisCharacteristic;
    }

    public void setNumberWithThisCharacteristic(int numberWithThisCharacteristic) {
        NumberWithThisCharacteristic = numberWithThisCharacteristic;
    }

    public int getPercent() {
        return percent;
    }

    public int compareTo(LogListObject that)
    {
        int result = 0;
        if(this.percent < that.percent)
            result = -1;
        if(this.percent > that.percent)
            result = 1;
        if(this.percent == that.percent)
            result = 0;
        return result;
    }

}
