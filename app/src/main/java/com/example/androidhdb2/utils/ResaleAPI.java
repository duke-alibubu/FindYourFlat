package com.example.androidhdb2.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.example.androidhdb2.activities.MainActivity;
import com.example.androidhdb2.model.ResaleFlat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class ResaleAPI {

//    private ArrayList<ResaleFlat> ResaleFlatArray = new ArrayList<ResaleFlat>();
    String[] Database_Flat_Type;
    String[] Database_Floor_Area_range;
    String[] Database_Remaining_Lease_range;
    String[] Database_Selling_Price_range;
    String[] Database_Storey_range;
    Boolean lock=false;

    public ResaleAPI(String[]database_Flat_Type, String[] database_Selling_Price_range, String[] database_Remaining_Lease_range, String[] database_Storey_range, String[] database_Floor_Area_range) {
        Database_Flat_Type = database_Flat_Type;
        Database_Floor_Area_range = database_Floor_Area_range;
        Database_Remaining_Lease_range = database_Remaining_Lease_range;
        Database_Selling_Price_range = database_Selling_Price_range;
        Database_Storey_range =  database_Storey_range;

    }


    public ArrayList<ResaleFlat> requestData(String url_region, String url_Flat_Type, String url_date_2019) {
        OkHttpClient client = new OkHttpClient();
        ArrayList<ResaleFlat> ResaleFlatArray = new ArrayList<ResaleFlat>();

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://data.gov.sg/api/action/datastore_search").newBuilder();
        urlBuilder.addQueryParameter("resource_id", "1b702208-44bf-4829-b620-4615ee19b57c");
        urlBuilder.addQueryParameter("q", "{\"town\":\"" + url_region +
                "\",\"flat_type\":\"" + url_Flat_Type +
                "\",\"month\":\"" + url_date_2019 +
                "\",\"month\":\"2018\"}");
        urlBuilder.addQueryParameter("limit", "1000000");
        //urlBuilder.addQueryParameter("q", "2018");
        String url = urlBuilder.build().toString();
        Log.d("RAPI", url + ' ' + url_region + ' ' + url_Flat_Type + ' ' + url_date_2019);

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("ResaleAPIr","Request failed");
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log.d("ResaleAPIr","Response received");
                try {
                    final String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONObject results = json.getJSONObject("result");
                        JSONArray jsonArray = results.getJSONArray("records");
                        // Check for null return
                        Log.d("ResaleAPIjson", String.valueOf(jsonArray));

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonArraypos = jsonArray.getJSONObject(i);
                            String town = jsonArraypos.getString("town");
                            String flat_type = jsonArraypos.getString("flat_type");
                            String resale_price = jsonArraypos.getString("resale_price");
                            String month = jsonArraypos.getString("month");
                            String remaining_lease = jsonArraypos.getString("remaining_lease");
                            String storey_range = jsonArraypos.getString("storey_range");
                            String floor_area = jsonArraypos.getString("floor_area_sqm");
                            String location = "Block " + jsonArraypos.getString("block") + " " + jsonArraypos.getString("street_name");
                            ResaleFlat rf = new ResaleFlat(String.valueOf(i), location, flat_type, Integer.parseInt(resale_price), Integer.parseInt(remaining_lease), storey_range, Float.parseFloat(floor_area));
                            Log.d("RESALEAPI", rf.toString());
                            ResaleFlatArray.add(rf);
                        }
                        lock=true;
                        return;



                        // Add check for compulsory fields here

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        try {
            // Wait for flatarray to load
            while (!lock)
                TimeUnit.MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("RESALEAPI", String.valueOf(ResaleFlatArray));
        return ResaleFlatArray;
    }

    public ArrayList<ResaleFlat> filterFlats(ArrayList<ResaleFlat> flats, String flat_type, String sellingprice, String lease_range, String storey_range, String floor_area_range) {
        ArrayList<ResaleFlat> relevantFlats = new ArrayList<ResaleFlat>();
        Log.d("filterflats", flat_type + ' ' + sellingprice + ' ' + lease_range);
        for (int i=0; i<flats.size(); i++) {
            Log.d("RelevantFlats", String.valueOf(compare_type(flats.get(i), flat_type))+ String.valueOf(compare_price(flats.get(i), sellingprice))+String.valueOf(compare_lease(flats.get(i), lease_range)));
            if (compare_type(flats.get(i), flat_type)
                    && compare_price(flats.get(i), sellingprice)
                    && compare_lease(flats.get(i), lease_range)
                    && compare_storey(flats.get(i), storey_range)
                    && compare_area(flats.get(i), floor_area_range)) {
                relevantFlats.add(flats.get(i));
            }
        }
        Log.d("RelevantFlats", String.valueOf(relevantFlats));
        return relevantFlats;
    }
    public boolean compare_type(ResaleFlat flat, String flat_type_detail) {
        if (flat.getFlatSize().equals(flat_type_detail))
            return true;
        else
            return false;

    }

    public boolean compare_price(ResaleFlat flat, String selling_price_range_detail) {
        int minprice = 0;
        int maxprice = 0;
        if (selling_price_range_detail.equals(Database_Selling_Price_range[0]))
            return true;
        Log.d("ResaleAPI", selling_price_range_detail+Database_Selling_Price_range[0]);
        if (selling_price_range_detail.equals(Database_Selling_Price_range[1])) {
            minprice = 1; maxprice = 200000;}
        else if (selling_price_range_detail.equals(Database_Selling_Price_range[2])) {
            minprice = 200001; maxprice = 400000;}
        else if (selling_price_range_detail.equals(Database_Selling_Price_range[3])) {
            minprice = 400001; maxprice = 600000;}
        else if (selling_price_range_detail.equals(Database_Selling_Price_range[4])) {
            minprice = 600001; maxprice = 800000;}
        else if (selling_price_range_detail.equals(Database_Selling_Price_range[5])) {
            minprice = 800001; maxprice = 1000000;}
        else if (selling_price_range_detail.equals(Database_Selling_Price_range[6])) {
            minprice = 1000001; maxprice = 10000000;}
        else {
            Log.e("ResaleAPI", "Invalid price range: "+selling_price_range_detail+ ' ' + Database_Selling_Price_range[0] + Database_Selling_Price_range[1]);
        }
        if (flat.getPrice() >= minprice && flat.getPrice() < maxprice)
                return true;
            else
                return false;

        }

    public boolean compare_lease(ResaleFlat flat, String remaining_lease_range_detail) {
        int minlease = 0;
        int maxlease = 0;

        if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[0])) {
            return true; }
        Log.d("ResaleAPI", remaining_lease_range_detail+Database_Remaining_Lease_range[0]);
        if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[1])) {
            minlease = 1; maxlease = 20;}
        else if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[2])) {
            minlease = 21; maxlease = 40;}
        else if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[3])) {
            minlease = 41; maxlease = 60;}
        else if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[4])) {
            minlease = 61; maxlease = 80;}
        else if (remaining_lease_range_detail.equals(Database_Remaining_Lease_range[5])) {
            minlease = 81; maxlease = 99;}
        else {
            Log.e("ResaleAPI", "Invalid lease range"+remaining_lease_range_detail+Database_Remaining_Lease_range[0]);
        }
        if (flat.getRemainingLease() >= minlease && flat.getRemainingLease() < maxlease)
            return true;
        else
            return false;
    }

    public boolean compare_area(ResaleFlat flat, String area_range_detail) {
        int minarea = 0;
        int maxarea = 0;
        if (area_range_detail.equals(Database_Floor_Area_range[0])) {
            return true;}
        if (area_range_detail.equals(Database_Floor_Area_range[1])) {
            minarea = 1; maxarea = 50;}
        else if (area_range_detail.equals(Database_Floor_Area_range[2])) {
            minarea = 51; maxarea = 100;}
        else if (area_range_detail.equals(Database_Floor_Area_range[3])) {
            minarea = 101; maxarea = 150;}
        else if (area_range_detail.equals(Database_Floor_Area_range[4])) {
            minarea = 151; maxarea = 200;}
        else if (area_range_detail.equals(Database_Floor_Area_range[5])) {
            minarea = 201; maxarea = 9999;}
        else {
            Log.e("ResaleAPI", "Invalid area range");
        }
        if (flat.getFloorArea() >= minarea && flat.getFloorArea() < maxarea)
            return true;
        else
            return false;
    }

    public boolean compare_storey(ResaleFlat flat, String storey_range_detail) {
        int minstorey = 0;
        int maxstorey = 0;
        if (storey_range_detail.equals(Database_Storey_range[0])) {
            return true;}
        if (storey_range_detail.equals(Database_Storey_range[1])) {
            minstorey = 1; maxstorey = 4;}
        else if (storey_range_detail.equals(Database_Storey_range[2])) {
            minstorey = 5; maxstorey = 10;}
        else if (storey_range_detail.equals(Database_Storey_range[3])) {
            minstorey = 11; maxstorey = 15;}
        else if (storey_range_detail.equals(Database_Storey_range[4])) {
            minstorey = 16; maxstorey = 200;}
        else {
            Log.e("ResaleAPI", "Invalid storey range");
        }
        if (Integer.parseInt(flat.getStorey().substring(0,2)) <= maxstorey && Integer.parseInt(flat.getStorey().substring(6,8)) >= minstorey)
            return true;
        else
            return false;
    }

}
