package com.example.androidhdb2.model;

import java.io.Serializable;

public class Flat implements Serializable {

    private String flatID;
    private String location;
    private String flatSize;

    public Flat(){ };

    public Flat(String flatID, String location, String flatSize) {
        this.flatID = flatID;
        this.location = location;
        this.flatSize = flatSize;
    }

    @Override
    public boolean equals(Object o) {
        return flatID.equals(((Flat) o).getFlatID());
    }

    public String getLocation() {
        return location;
    }

    public String getFlatSize() {
        return flatSize;
    }

    public String getFlatID() {
        return flatID;
    }

    public static class BtoFlat {

    }


}
