package com.example.androidhdb2.utils;

import android.content.Context;
import android.util.Log;

import com.example.androidhdb2.R;
import com.example.androidhdb2.model.SBFlat;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class SBFDataProvider {

    public static final String TAG = "DataProvider2";
    public FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void readCSV(Context mContext) {
        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.sbf2);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            String nextLine;
            String uid;
            String region;
            String flatSize;
            int flatSupply;
            Map<String, Integer> ethnicQuota = new HashMap<>();
            int price;
            int count = 101;

            while ((nextLine = reader.readLine()) != null) {
                Log.d(TAG, nextLine);

                uid = String.valueOf(count);
                String[] lines = nextLine.split(",");
                region = lines[0];
                flatSize = lines[1];
                flatSupply = Integer.parseInt(lines[2]);
                ethnicQuota.put("C",Integer.parseInt(lines[3]));
                ethnicQuota.put("M",Integer.parseInt(lines[4]));
                ethnicQuota.put("O",Integer.parseInt(lines[5]));
                price = Integer.parseInt(lines[6]);

                SBFlat flat = new SBFlat(uid, region, flatSize, price, flatSupply, ethnicQuota, region);

                db.collection("SBFlat").document(uid).set(flat);
                count++;


            }


        }catch (Exception e) {
            Log.d(TAG, "error" + e.getMessage());
        }
    }
}