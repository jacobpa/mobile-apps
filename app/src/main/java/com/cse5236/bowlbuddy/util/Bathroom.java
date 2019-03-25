package com.cse5236.bowlbuddy.util;

import android.util.Log;

public class Bathroom {
    private static final String TAG = "BathroomModel";

    private int stallCount;
    private int sinkCount;
    private int plyCount;
    private int cleanRating;
    private int smellRating;
    private int emptyRating;
    private boolean paperTowels;
    private boolean electricDryer;
    private boolean handicap;
    private String gender;
    private Location location;

    /**
     * Constructor with required fields.
     * @param cleanRating Rating out of five of how clean a bathroom is
     * @param smellRating Rating out of five of how good a bathroom smells
     * @param emptyRating Rating out of five of how empty a bathroom usually is
     * @param gender One of "Male", "Female", "Unisex", "Other"
     */
    public Bathroom(int cleanRating, int smellRating, int emptyRating, String gender, Location location) {
        this.cleanRating = cleanRating;
        this.smellRating = smellRating;
        this.emptyRating = emptyRating;
        this.gender = gender;
        this.location = location;
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

    public int getPlyCount() {
        return plyCount;
    }

    public void setPlyCount(int plyCount) {
        if(plyCount < 0 || plyCount > 2) {
            Log.e(TAG, "setPlyCount: Invalid ply count.");
        } else {
            this.plyCount = plyCount;
        }
    }

    public int getCleanRating() {
        return cleanRating;
    }

    public void setCleanRating(int cleanRating) {
        this.cleanRating = cleanRating;
    }

    public int getSmellRating() {
        return smellRating;
    }

    public void setSmellRating(int smellRating) {
        this.smellRating = smellRating;
    }

    public int getEmptyRating() {
        return emptyRating;
    }

    public void setEmptyRating(int emptyRating) {
        this.emptyRating = emptyRating;
    }

    public float getAverageRating() {
        return (this.emptyRating + this.smellRating + this.cleanRating) / 3.0f;
    }

    public boolean isPaperTowels() {
        return paperTowels;
    }

    public void setPaperTowels(boolean paperTowels) {
        this.paperTowels = paperTowels;
    }

    public boolean isElectricDryer() {
        return electricDryer;
    }

    public void setElectricDryer(boolean electricDryer) {
        this.electricDryer = electricDryer;
    }

    public boolean isHandicap() {
        return handicap;
    }

    public void setHandicap(boolean handicap) {
        this.handicap = handicap;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
