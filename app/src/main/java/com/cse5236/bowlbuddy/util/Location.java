package com.cse5236.bowlbuddy.util;

import java.util.Locale;

public class Location {
    private int floor;
    private int roomNum;
    private Building building;

    public Location(int floor, int roomNum, Building building) {
        this.floor = floor;
        this.roomNum = roomNum;
        this.building = building;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "Room %d, floor %d", roomNum, floor);
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }
}
