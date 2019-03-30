package com.example.androidhdb2.utils;

import com.example.androidhdb2.model.PastBtoFlat;
import com.example.androidhdb2.model.SBFlat;

import java.util.ArrayList;

public class SBFilter {
    public static ArrayList filterFlats(String[] detail) {
        String flatType = detail[0];
        String priceRange = detail[1];
        String flatSupplyRange = detail[2];
        String ethnicGroup = detail[3];
        String ethnicGroupQuota = detail[4];

        ArrayList sa = new ArrayList();
        sa.add(checkFlatType(flatType));
        sa.add(checkPriceRange(priceRange));
        sa.add(checkFlatSupplyRange(flatSupplyRange));
        sa.add(checkEthnicGroup(ethnicGroup));
        sa.add(checkEthnicGroupQuota(ethnicGroupQuota));
        return sa;

    }

    private static String checkFlatType(String flatType) {
        if (flatType.equals("2 ROOM FLEXI")) {
            return "2-room Flexi";
        }
        else if (flatType.equals("3 ROOM")) {
            return "3-room";
        }
        else if (flatType.equals("4 ROOM")) {
            return "4-room";
        }
        else if (flatType.equals("5 ROOM")) {
            return "5-room";
        }
        else
            return "Executive";
    }

    private static String[] checkPriceRange(String priceRange) {
        String minprice;
        String maxprice;
        if (priceRange.equals("Choose Selling Price Range")) {
            return null;
        }
        else if (priceRange.equals("$1 to $200,000")) {
            minprice = "1";
            maxprice = "200000";
        }
        else if (priceRange.equals("$200,001 to $400,000")) {
            minprice = "200001";
            maxprice = "400000";
        }
        else if (priceRange.equals("$400,001 to $600,000")) {
            minprice = "400001";
            maxprice = "600000";
        }
        else if (priceRange.equals("$600,001 to $800,000")) {
            minprice = "600001";
            maxprice = "800000";
        }
        else {
            minprice = "800000";
            maxprice = "1000000";
        }
        String [] sa = new String[2];
        sa[0] = minprice;
        sa[1] = maxprice;
        return sa;
    }

    private static String[] checkFlatSupplyRange(String fsRange) {
        String[] sa = new String[2];
        if (fsRange.equals("Choose Flat Supply Range")) {
            return null;
        }
        if (fsRange.equals("More than 200")) {
            sa[0] = "200";
            sa[1] = "10000000";
        }
        else {
            sa = fsRange.split(" to ");
        }
        return sa;
    }

    private static String checkEthnicGroup(String eg) {
        if (eg.equals("Choose Ethnic")) {
            return null;
        }
        else if (eg.equals("Chinese")) {
            return "C";
        }
        else if (eg.equals("Malay")) {
            return "M";
        }
        else if(eg.equals("Others"))
            return "O";
        else
            return null;
    }

    private static String[] checkEthnicGroupQuota(String egq) {
        String[] sa = new String[2];
        if (egq.equals("Select Ethnic Group Quota Range")){
            return null;
        }
        if (egq.equals("More than 200")) {
            sa[0] = "200";
            sa[1] = "100000";
        }
        else {
            sa = egq.split(" to ");
        }
        return sa;
    }
}
