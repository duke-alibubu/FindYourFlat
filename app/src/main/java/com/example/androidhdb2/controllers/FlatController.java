package com.example.androidhdb2.controllers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.androidhdb2.model.PastBtoFlat;
import com.example.androidhdb2.model.ResaleFlat;
import com.example.androidhdb2.model.SBFlat;
import com.example.androidhdb2.model.UpcomingBtoFlat;
import com.example.androidhdb2.utils.ResaleAPI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FlatController {
    private String TAG = "FLATCONTROLLER";

//
    public List<SBFlat> getSBF(List<SBFlat> flatArrayList, ArrayList results) {
        String TAG = "Flat Controller";
        String flatType;
        String[] priceRange = new String[2];
        String[] flatSupplyRange = new String[2];
        String ethnicGroup="";
        String[] ethnicGroupQuota = new String[2];

        flatType = (String) results.get(0);
        if (results.get(1) != null) {
            priceRange = (String[]) results.get(1);
            Log.d(TAG, priceRange[0]);
        }
        if (results.get(2) != null)
            flatSupplyRange = (String[]) results.get(2);
        if (results.get(3)!=null) {
            ethnicGroup = (String) results.get(3);
            ethnicGroupQuota = (String[]) results.get(4);
        }
        Log.d(TAG, flatType);

        String finalethnicGroup = ethnicGroup;
        String[] finalPriceRange = priceRange;
        Log.d(TAG, "fPR"+finalPriceRange);
        String[] finalFlatSupplyRange = flatSupplyRange;
        String[] finalEthnicGroupQuota = ethnicGroupQuota;

        for (SBFlat flat : flatArrayList) {
            if (!compare_FT(flat, flatType))
                flatArrayList.remove(flat);
            if (finalPriceRange[0] != null) {
                if (!compare_PR(flat, Integer.parseInt(finalPriceRange[0]), Integer.parseInt(finalPriceRange[1])))
                    flatArrayList.remove(flat);
            }
            if (finalFlatSupplyRange[0] != null && !compare_SR(flat, Integer.parseInt(finalFlatSupplyRange[0]), Integer.parseInt(finalFlatSupplyRange[1]))) {
                flatArrayList.remove(flat);
            }
            if (finalethnicGroup != "" && !compare_EG(flat, finalethnicGroup, Integer.parseInt(finalEthnicGroupQuota[0]), Integer.parseInt(finalEthnicGroupQuota[1]))) {
                flatArrayList.remove(flat);
            }
        }
        return flatArrayList;
    }
//
    public ArrayList<ResaleFlat> getResale(String[] flatList, String[] priceList, String[] leaseList, String[] storeyList, String[] areaList,
                                           String flatType, String priceRange, String remainingLeaseRange, String storeyRange, String floorAreaRange, String region) {
        ResaleAPI api = new ResaleAPI(flatList, priceList, leaseList, storeyList, areaList);
        ArrayList<ResaleFlat> resaleFlatArrayListList = api.requestData(region, flatType, "2019");
        Log.d("Flatcontroller", String.valueOf(resaleFlatArrayListList));
        return api.filterFlats(resaleFlatArrayListList, flatType, priceRange, remainingLeaseRange, storeyRange, floorAreaRange);
    }
//
//    public ArrayList<UpcomingBtoFlat> getUpcomingBTO() {
//
//    }

    public boolean compare_FT(SBFlat flat, String ft) {
        if (flat.getFlatSize().contains(ft))
            return true;
        return false;
    }

    public boolean compare_SR(SBFlat flat, int min, int max) {
        if (flat.getFlatSupply() > max || flat.getFlatSupply()<min) {
            return false;
        } return true;
    }

    public boolean compare_PR(SBFlat flat, int min, int max) {
        if (flat.getPrice() > max || flat.getPrice() < min)
            return false;
        return true;
    }

    public boolean compare_EG(SBFlat flat, String ethnicGroup, int min, int max) {
        if (flat.getEthnicQuota(ethnicGroup) < min || flat.getEthnicQuota(ethnicGroup) > max)
            return false;
        return true;
    }

}
