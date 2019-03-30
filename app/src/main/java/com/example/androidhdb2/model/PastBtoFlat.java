package com.example.androidhdb2.model;

// PAST LAUNCHES //

import java.io.Serializable;

public class PastBtoFlat extends Flat implements Serializable {
    private String image;
    private int price;
    private String region;

    public PastBtoFlat() {}

    public PastBtoFlat(String flatID, String location, String flatSize, int price, String image, String region) {
        super(flatID, location, flatSize);
        this.price = price;
        this.image = image;
        this.region = region;
    }

    public String getImage() {
        return image;
    }

    public int getPrice() {
        return price;
    }

    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "PastBtoFlat{" +
                "image='" + image + '\'' +
                ", price=" + price +
                ", region='" + region + '\'' +
                '}';
    }
}
