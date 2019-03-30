package com.example.androidhdb2.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.androidhdb2.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class BtoInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private Context mContext;


    public BtoInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.bto_flat_info_window, null);
    }

    private void addInfo(Marker marker, View view){
        String title = marker.getTitle();
        TextView tvTitle = view.findViewById(R.id.title);

        if (!title.equals("")) {
            tvTitle.setText(title);
        }

        String flatInfo = marker.getSnippet();
        TextView tvFlatInfo = view.findViewById(R.id.flatInfo);
        if (! flatInfo.equals("")){
            tvFlatInfo.setText(flatInfo);
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        addInfo(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        addInfo(marker, mWindow);
        return mWindow;
    }
}
