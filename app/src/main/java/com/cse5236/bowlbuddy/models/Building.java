package com.cse5236.bowlbuddy.models;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.Objects;

public class Building implements Serializable {
    private Integer id;
    private Integer floors;
    private String address;
    private String name;
    @Json(name = "open_time")
    private String openTime;
    @Json(name = "close_time")
    private String closeTime;
    private Double longitude;
    private Double latitude;

    public Building() {
    }

    public Building(Integer id, Integer floors, String address, String name, String openTime, String closeTime, Double longitude, Double latitude) {
        this.id = id;
        this.floors = floors;
        this.address = address;
        this.name = name;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String toString() {
        return name;
    }

    public Integer getId() {return id;}

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return Objects.equals(((Building) obj).getId(), this.id);
    }
}
