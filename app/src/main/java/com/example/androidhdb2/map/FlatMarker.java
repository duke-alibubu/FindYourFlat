package com.example.androidhdb2.map;

import com.example.androidhdb2.model.Flat;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class FlatMarker implements ClusterItem {
    private LatLng position;
    private String title;
    private String snippet;
    private int iconPic;
    private Flat flat;

    public FlatMarker(LatLng position, String title, String snippet, int iconPic, Flat flat) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPic = iconPic;
        this.flat = flat;
    }


    public FlatMarker() {
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public int getIconPic() {
        return iconPic;
    }

    public void setIconPic(int iconPic) {
        this.iconPic = iconPic;
    }

    public Flat getFlat() { return flat; }
}
