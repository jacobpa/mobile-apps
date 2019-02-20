package com.cse5236.bowlbuddy.util;

public class Building {
    private int floors;
    private String name;
    private String address;

    public Building(int floors, String name, String address) {
        this.floors = floors;
        this.name = name;
        this.address = address;
    }

    public int getFloors() {
        return floors;
    }

    public void setFloors(int floors) {
        this.floors = floors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
