package com.vanikad.hw.londonlivetraffic;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.vanikad.hw.londonlivetraffic.database.DictionaryDatabase;
import com.vanikad.hw.londonlivetraffic.dialogs.AboutDialog;
import com.vanikad.hw.londonlivetraffic.dialogs.CamImageDialog;
import com.vanikad.hw.londonlivetraffic.dialogs.ListCamerasDialog;
import com.vanikad.hw.londonlivetraffic.dialogs.ListFavoritesDialog;
import com.vanikad.hw.londonlivetraffic.dialogs.MapTypeDialog;
import com.vanikad.hw.londonlivetraffic.map.InfoMarkerRenderer;
import com.vanikad.hw.londonlivetraffic.map.MyClusterItem;
import com.vanikad.hw.londonlivetraffic.network.AsyncResponse;
import com.vanikad.hw.londonlivetraffic.network.VolleySingleton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements AsyncResponse, GoogleMap.OnMapLoadedCallback, GoogleMap.CancelableCallback, GoogleMap.OnCameraChangeListener {

    Context context;
    private GoogleMap googleMap;
    ImageButton favsButton;
    ImageButton listCamerasButton;
    ImageButton trafficLayerButton;

    // Declare a variable for the cluster manager.
    ClusterManager<MyClusterItem> mClusterManager;

    CamListXmlParser camListXmlParser;
    List<LatLng> latLngs;
    SharedPreferences prefs = null;
    CamImageDialog camImageDialog;
    ListFavoritesDialog listFavoritesDialog;
    ListCamerasDialog listCamerasDialog;
    DictionaryDatabase mDictionaryDatabase;
    private long rowid;
    String[] xmlData = {AppData.SOURCE_XML_URL, AppData.XML_FILE_NAME};

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        private static final String TAG = "NetworkStateReceiver";
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Network connectivity change");
            if (intent.getExtras() != null) {
                final ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    Log.i(TAG, "Network " + ni.getTypeName() + " connected");
                    reinitializeDataAndMap();
                } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                    Log.d(TAG, "There's no network connectivity");
                    Toast.makeText(context, "Internet connection is lost", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(com.vanikad.hw.londonlivetraffic.R.layout.activity_main);
        context = MainActivity.this;
        prefs = getSharedPreferences(AppData.PREFERENCES, Context.MODE_PRIVATE);
        camListXmlParser = new CamListXmlParser(context);
        latLngs = new ArrayList<>();

        if (!CommonHelper.isOnline(context)) {
            Toast toast = Toast.makeText(context,
                    "Please enable internet connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
        }

        initializeMap();

        if (!CommonHelper.isFilePresent(AppData.XML_FILE_NAME, context)) {
            CommonHelper.loadFile(xmlData, context, this);
        } else {
            setDatabase();
            setUpClusterer();
        }

        initializeToolbar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camImageDialog != null){
            camImageDialog.stopTimer();
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camImageDialog != null){
            if (camImageDialog.isAutoRefreshEnabled()){
                camImageDialog.startTimer();
            }
        }
        registerReceiver(broadcastReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void initializeMap() {
        googleMap = CommonHelper.getMap(this);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setMyLocationEnabled(true);
    }

    public void setUpClusterer() {
        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MyClusterItem>(context, googleMap);
        mClusterManager.setRenderer(new InfoMarkerRenderer(context, googleMap, mClusterManager));
        NonHierarchicalDistanceBasedAlgorithm distanceBasedAlgorithm = new NonHierarchicalDistanceBasedAlgorithm();
        mClusterManager.setAlgorithm(new PreCachingAlgorithmDecorator<MyClusterItem>(distanceBasedAlgorithm));
        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnMarkerClickListener(mClusterManager);
        googleMap.setOnCameraChangeListener(mClusterManager);

//        Add cluster items (markers) to the cluster manager.
        addClusterItems();

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyClusterItem> myClusterItemCluster) {
                float zoom = googleMap.getCameraPosition().zoom;
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myClusterItemCluster.getPosition(), zoom + 2));
                return true;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyClusterItem>() {
            @Override
            public boolean onClusterItemClick(MyClusterItem myClusterItem) {
                VolleySingleton.getInstance(context.getApplicationContext()).getRequestQueue().getCache().clear();
                rowid = myClusterItem.getRowid();
                showPhotoDialog(context, rowid);
                return true;
            }
        });
        loadPreferences();
    }

    public void addClusterItems() {

        DictionaryDatabase dictionaryDatabase = new DictionaryDatabase(MainActivity.this);
        Cursor cursor = dictionaryDatabase.queryAllEntries(null);
        while (cursor.moveToNext()) {

            int latIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_LATITUDE);
            int lngIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_LONGITUDE);
            int rowidIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);

            double lat = Double.parseDouble(cursor.getString(latIndex));
            double lng = Double.parseDouble(cursor.getString(lngIndex));
            long rowid = cursor.getInt(rowidIndex);

            MyClusterItem myClusterItem = new MyClusterItem(
                    lat, lng, rowid);
            mClusterManager.addItem(myClusterItem);

            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.vanikad.hw.londonlivetraffic.R.menu.menu_main, menu);

//        Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(com.vanikad.hw.londonlivetraffic.R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case com.vanikad.hw.londonlivetraffic.R.id.search:
                onSearchRequested();
                return true;
            case com.vanikad.hw.londonlivetraffic.R.id.maptype:
                showMapTypeDialog();
                return true;
            case com.vanikad.hw.londonlivetraffic.R.id.about:
                showAboutDialog();
                return true;
            case com.vanikad.hw.londonlivetraffic.R.id.close:
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    private void showMapTypeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        MapTypeDialog mapTypeDialog = new MapTypeDialog();
        mapTypeDialog.setArguments(context, googleMap);
        mapTypeDialog.show(fm, "mapTypeDialog");
    }

    private void showAboutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(fm, "aboutDialog");
    }

    public void showPhotoDialog(Context context, long rowid) {
        FragmentManager fm = getSupportFragmentManager();
        camImageDialog = new CamImageDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(AppData.ROWID, rowid);
        camImageDialog.setArguments(bundle);
        camImageDialog.setContext(context);
        camImageDialog.setRetainInstance(true);
        camImageDialog.show(fm, "camImageDialog");
    }

    public void showFavsDialog(){
        FragmentManager fm = getSupportFragmentManager();
        listFavoritesDialog = new ListFavoritesDialog();
        listFavoritesDialog.setContext(context);
        listFavoritesDialog.setRetainInstance(true);
        listFavoritesDialog.show(fm, "listFavoritesDialog");
    }

    public void showCamerasDialog(){
        FragmentManager fm = getSupportFragmentManager();
        listCamerasDialog = new ListCamerasDialog();
        listCamerasDialog.setContext(context);
        listCamerasDialog.setRetainInstance(true);
        listCamerasDialog.show(fm, "listFavoritesDialog");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Because this activity has set launchMode="singleTop", the system calls this method
        // to deliver the intent if this activity is currently the foreground activity when
        // invoked again (when the user executes a search from this activity, we don't create
        // a new instance of this activity, so the system delivers the search intent here)

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();

                int latIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_LATITUDE);
                int lngIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_LONGITUDE);
                int rowidIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);

                double lat = Double.parseDouble(cursor.getString(latIndex));
                double lng = Double.parseDouble(cursor.getString(lngIndex));

                this.rowid = cursor.getInt(rowidIndex);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17), this);
            }
        }


    }


    @Override
    public void downloadFinished(boolean result) {
        setDatabase();
        setUpClusterer();
    }

    @Override
    public void onFinish() {
        VolleySingleton.getInstance(context.getApplicationContext()).getRequestQueue().getCache().clear();
        showPhotoDialog(context, rowid);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    public void savePreferences() {
        prefs.edit().putInt(AppData.MAP_TYPE, googleMap.getMapType()).commit();
        prefs.edit().putBoolean(AppData.TRAFFIC_LAYER, googleMap.isTrafficEnabled()).commit();
        prefs.edit().putFloat(AppData.MAP_ZOOM_LEVEL, googleMap.getCameraPosition().zoom).commit();
        prefs.edit().putString(AppData.MAP_LATITUDE, Double.toString(googleMap.getCameraPosition().target.latitude)).commit();
        prefs.edit().putString(AppData.MAP_LONGITUDE, Double.toString(googleMap.getCameraPosition().target.longitude)).commit();
    }

    public void loadPreferences() {
        String defaultLatitude = "51.51986";
        String defaultLongitude = "-0.055646863";
        float defaultZoom = 9;
        int defaultMapType = GoogleMap.MAP_TYPE_NORMAL;
        Double savedLatitude = Double.parseDouble(prefs.getString(AppData.MAP_LATITUDE, defaultLatitude));
        Double savedLongitude = Double.parseDouble(prefs.getString(AppData.MAP_LONGITUDE, defaultLongitude));
        float savedZoom = prefs.getFloat(AppData.MAP_ZOOM_LEVEL, defaultZoom);
//        isTrafficLayerEnabled = prefs.getBoolean(AppData.TRAFFIC_LAYER, true);

        googleMap.setTrafficEnabled(prefs.getBoolean(AppData.TRAFFIC_LAYER, true));
        googleMap.setMapType(prefs.getInt(AppData.MAP_TYPE, defaultMapType));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(savedLatitude, savedLongitude), savedZoom));
    }

    private void initializeToolbar(){
        favsButton = (ImageButton)findViewById(R.id.favoriteMainButton);
        favsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFavsDialog();
            }
        });
        listCamerasButton = (ImageButton)findViewById(R.id.listCamerasButton);
        listCamerasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCamerasDialog();
            }
        });
        trafficLayerButton = (ImageButton)findViewById(R.id.trafficLayerEnable);
        if (prefs.getBoolean(AppData.TRAFFIC_LAYER, true)){
            trafficLayerButton.setImageResource(R.drawable.traffic_color);
        } else {
            trafficLayerButton.setImageResource(R.drawable.traffic);
        }
        trafficLayerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleMap.isTrafficEnabled()){
                    googleMap.setTrafficEnabled(false);
                    trafficLayerButton.setImageResource(R.drawable.traffic);
                    Toast.makeText(context, "Traffic layer turned OFF", Toast.LENGTH_LONG).show();
                } else {
                    googleMap.setTrafficEnabled(true);
                    trafficLayerButton.setImageResource(R.drawable.traffic_color);
                    Toast.makeText(context, "Traffic layer turned ON", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onMapLoaded() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void setDatabase(){
        mDictionaryDatabase = new DictionaryDatabase(MainActivity.this);
        mDictionaryDatabase.getDatabase().close();
    }

    private void reinitializeDataAndMap(){
        if (!CommonHelper.isFilePresent(AppData.XML_FILE_NAME, context)) {
            CommonHelper.loadFile(xmlData, context, this);
        }
    }
}