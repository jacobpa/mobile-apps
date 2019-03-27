package com.cse5236.bowlbuddy.models;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private int karma;

    public User() {
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

    /**
     * Determines if a certain username is valid.
     *
     * @param username The username to check the validity of
     * @return True if valid, false otherwise
     */
    public static boolean isUsernameValid(String username) {
        String usernameRegexp = "[A-Za-z0-9_]+";

        return username.matches(usernameRegexp);
    }
}
