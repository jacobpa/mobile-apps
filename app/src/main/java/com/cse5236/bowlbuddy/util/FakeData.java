package com.cse5236.bowlbuddy.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.bloco.faker.Faker;

public class FakeData {
    private final static String TAG = "FakeData";
    private final static String[] GENDERS = {"Male", "Female", "Unisex", "Other"};

    private List<Building> buildings;
    private List<Location> locations;
    private List<Bathroom> bathrooms;
    private Faker faker;
    private Random rNumGenerator;

    public FakeData() {
        faker = new Faker();
        rNumGenerator = new Random();
        buildings = new ArrayList<>();
        locations = new ArrayList<>();
        bathrooms = new ArrayList<>();
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public List<Bathroom> getBathrooms() {
        return bathrooms;
    }

    public void generateData(int buildingCount) {
        generateBuildings(buildingCount);
        generateLocations();
        generateBathrooms();
    }

    /**
     * Generate a number of Buildings.
     * @param count The number of buildings to generate
     */
    private void generateBuildings(int count) {
        for(int i = 0; i < count; i++) {
            buildings.add(new Building(
                    rNumGenerator.nextInt(29) + 1,
                    faker.name.name(),
                    faker.address.streetAddress()
            ));
        }
        Log.d(TAG, "generateBuildings: Generated " + count + " buildings.");
    }

    private void generateLocations() {
        int totalLocations = 0;
        for(Building building : buildings) {
            int locationCount = rNumGenerator.nextInt(4) + 1;
            for(int i = 0; i < locationCount; i++) {
                locations.add(new Location(
                        rNumGenerator.nextInt(29) + 1,
                        rNumGenerator.nextInt(9998) + 1,
                        building
                ));
            }
            totalLocations += locationCount;
        }

        Log.d(TAG, "generateBuildings: Generated " + totalLocations + " locations.");
    }

    private void generateBathrooms() {
        for(Location location : locations) {
            bathrooms.add(new Bathroom(
                    rNumGenerator.nextInt(6),
                    rNumGenerator.nextInt(6),
                    rNumGenerator.nextInt(6),
                    GENDERS[rNumGenerator.nextInt(GENDERS.length)],
                    location
            ));
        }

        Log.d(TAG, "generateBuildings: Generated " + locations.size() + " bathrooms.");
    }
}
