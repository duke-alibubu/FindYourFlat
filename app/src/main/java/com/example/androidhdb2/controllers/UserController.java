package com.example.androidhdb2.controllers;

import android.content.Context;
import android.util.Log;

import com.example.androidhdb2.model.Bookmark;
import com.example.androidhdb2.model.Flat;
import com.example.androidhdb2.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class UserController {
    public static User importUser(Context mContext, File filesdir, String userid) {

        File file = new File(filesdir, userid);
            try {
                FileInputStream fi = mContext.openFileInput(userid);
                ObjectInputStream oi = new ObjectInputStream(fi);
                User user = (User) oi.readObject();
                oi.close();
                fi.close();

                return user;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        return null;
        }



    public static void addUserBookmark(Context mContext,File filesdir, String userid, Flat flat) {
        File file = new File(filesdir, userid);
        User user = importUser(mContext , filesdir, userid);
        user.addBookmark(new Bookmark(flat));
        file.delete();

        // Overwrites the original file
        try {
            FileOutputStream fileOutputStream = mContext.openFileOutput(userid, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void removeUserBookmark(Context mContext, File filesdir, String userid, Flat flat) {
        File file = new File(filesdir, userid);
        User user = importUser(mContext, filesdir, userid);
        user.removeBookmark(new Bookmark(flat));
        file.delete();

        // Overwrites the original file
        try {
            FileOutputStream fileOutputStream = mContext.openFileOutput(userid, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
