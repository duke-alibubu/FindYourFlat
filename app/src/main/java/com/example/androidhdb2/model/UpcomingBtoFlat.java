package com.example.androidhdb2.model;

// NEW LAUNCHES //

import java.io.Serializable;

public class UpcomingBtoFlat extends Flat implements Serializable {
    private String image;
    private String total;

    public UpcomingBtoFlat() {}

    public UpcomingBtoFlat(String flatID, String loc, String flatSize, String image, String total) {
        super(flatID, loc, flatSize);
        this.image = image;
        this.total = total;
    }

    public String getImage() {
        return image;
    }

    public String getTotal() { return total; }
}
