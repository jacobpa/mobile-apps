package com.cse5236.bowlbuddy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Bathroom {
    private int stallCount;
    private int sinkCount;
    private boolean electricDryer;
    private List<Date> peakTime;
    private boolean handicap;
    private int plyCount;

    public Bathroom() {
        stallCount = 0;
        sinkCount = 0;
        electricDryer = false;
        peakTime = new ArrayList<>(2);
        handicap = false;
        plyCount = 1;
    }

    public int getStallCount() {
        return stallCount;
    }

    public void setStallCount(int stallCount) {
        this.stallCount = stallCount;
    }

    public int getSinkCount() {
        return sinkCount;
    }

    public void setSinkCount(int sinkCount) {
        this.sinkCount = sinkCount;
    }

    public boolean isElectricDryer() {
        return electricDryer;
    }

    public void setElectricDryer(boolean electricDryer) {
        this.electricDryer = electricDryer;
    }

    public List<Date> getPeakTime() {
        return peakTime;
    }

    public void setPeakTime(List<Date> peakTime) {
        this.peakTime = peakTime;
    }

    public boolean isHandicap() {
        return handicap;
    }

    public void setHandicap(boolean handicap) {
        this.handicap = handicap;
    }

    public int getPlyCount() {
        return plyCount;
    }

    public void setPlyCount(int plyCount) {
        this.plyCount = plyCount;
    }
}
