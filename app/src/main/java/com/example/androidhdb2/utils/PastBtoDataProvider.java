package com.example.androidhdb2.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.androidhdb2.R;
import com.example.androidhdb2.model.PastBtoFlat;
import com.example.androidhdb2.model.UpcomingBtoFlat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class PastBtoDataProvider {

    public static final String TAG = "DataProvider";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void readCSV(Context mContext) {
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.fullbto);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            String nextLine;
            String region;
            String estate;
            String flatSize;
            int price;
            int count = 1;
            String uid;

            // Skip headers
            while ((nextLine = reader.readLine()) != null) {
                    // Log.d(TAG, nextLine);

                    // Split nextLine and assign variables
                    uid = String.format("%03d", count);
                    String[] lines = nextLine.split(",");
                    region = lines[0];
                    estate = lines[1];
                    flatSize = lines[2];
                    price = Integer.parseInt(lines[3].substring(7) + "000");

                    // Create flat object
                     PastBtoFlat flat = new PastBtoFlat(uid, estate, flatSize, price, "image", region);
//                    Map<String, Object> flat = new HashMap<>();
//                    flat.put("uid", uid);
//                    flat.put("estate", estate);
//                    flat.put("flatSize", flatSize);
//                    flat.put("price", price);
//                    flat.put("image", "image");
//                    flat.put("region", "region");

                    // Create document containing flat object
                    db.collection("PastBTO").document(uid).set(flat);
                    count++;
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
