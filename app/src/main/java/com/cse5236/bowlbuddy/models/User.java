package com.cse5236.bowlbuddy.models;

public class User {
    private int id;
    private String username;
    private int karma;

    public User(){
        //Empty Constructor
    }

    public User(int id, String username, int karma) {
        this.id = id;
        this.username = username;
        this.karma = karma;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }
}
