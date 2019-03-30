package com.example.androidhdb2.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SBFlat extends Flat implements Serializable {
    private int flatSupply;
    private int price;
    private Map<String, Integer> ethnicQuota;
    private String region;

    public SBFlat(){}

    public SBFlat(String flatID, String location, String flatSize, int price, int flatSupply, Map<String, Integer> ethnicQuota, String region) {
        super(flatID, location, flatSize);
        this.price = price;
        this.flatSupply = flatSupply;
        this.ethnicQuota = ethnicQuota;
        this.region = region;
    }

    public int getFlatSupply() {
        return flatSupply;
    }

    public int getEthnicQuota(String ethnicity) {
        return ethnicQuota.get(ethnicity);
    }

    public int getPrice() {
        return price;
    }

    public Map<String, Integer> getEthnicQuota() {
        return ethnicQuota;
    }

    public String getRegion() {
        return region;
    }
}
