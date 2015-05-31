package com.vanikad.hw.londonlivetraffic.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.vanikad.hw.londonlivetraffic.R;

/**
 * Created by HW on 5/6/2015.
 */
public class InfoMarkerRenderer extends DefaultClusterRenderer<MyClusterItem> {


    public InfoMarkerRenderer(Context context, GoogleMap map, ClusterManager<MyClusterItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MyClusterItem item,
                                               MarkerOptions markerOptions) {

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.camera));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        return cluster.getSize() > 5; // if markers <=5 then not clustering
    }



}