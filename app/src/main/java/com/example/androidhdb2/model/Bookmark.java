package com.example.androidhdb2.model;

import java.io.Serializable;

public class Bookmark implements Serializable {
    private Flat flat;
//    private String userID;

    public Bookmark(Flat flat) {
        this.flat = flat;
//        this.userID = userID;
    }

    public Flat getFlat() {
        return flat;
    }

//    public String getUserID() {
//        return userID;
//    }

    @Override
    public boolean equals(Object o) {
        return flat.equals(((Bookmark) o).getFlat());
    }
}
