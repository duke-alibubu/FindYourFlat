package com.example.androidhdb2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    private String userID;
    private ArrayList<Bookmark> bookmarkList;

    public User(){}

    public User(String userID, ArrayList<Bookmark> bookmarkList) {
        this.userID = userID;
        this.bookmarkList = bookmarkList;
    }

    public String getUserID() {
        return userID;
    }

    public ArrayList<Bookmark> getBookmarkList() {
        return bookmarkList;
    }

    public boolean addBookmark(Bookmark bookmark) {

        // If bookmark gets more attributes, this component will have to be edited
        if (bookmarkList.contains(bookmark)) {
            return false;
        } else {
            bookmarkList.add(bookmark);
            return true;
        }
    }

    public boolean removeBookmark(Bookmark bookmark) {

        if (bookmarkList.contains(bookmark)) {
            bookmarkList.remove(bookmark);
            return true;
        } else {
            return false;
        }
    }

}
