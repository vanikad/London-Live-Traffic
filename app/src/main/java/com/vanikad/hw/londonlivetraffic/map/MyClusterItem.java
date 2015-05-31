package com.vanikad.hw.londonlivetraffic.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by HW on 5/6/2015.
 */
public class MyClusterItem implements ClusterItem {

    private final LatLng mPosition;
    protected MarkerOptions marker;
    private long rowid;

    public MyClusterItem(double lat, double lng, long rowid){
        mPosition = new LatLng(lat, lng);
        this.rowid = rowid;
        this.marker = new MarkerOptions();
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public MarkerOptions getMarker() {
        return marker;
    }

    public void setMarker(MarkerOptions marker) {
        this.marker = marker;
    }

    public long getRowid(){return rowid;}

}


