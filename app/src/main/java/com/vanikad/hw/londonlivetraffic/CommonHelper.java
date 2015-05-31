package com.vanikad.hw.londonlivetraffic;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.vanikad.hw.londonlivetraffic.network.AsyncResponse;
import com.vanikad.hw.londonlivetraffic.network.FileDownloader;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by HW on 5/6/2015.
 */
public class CommonHelper {

    private static List<CamListXmlParser.Cam> camList;
    private static CamListXmlParser camListXmlParser;

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean isFilePresent(String fileName, Context context) {
        String[] files = context.getFilesDir().list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    public static void loadFile(String[] fileData, Context context, AsyncResponse delegate) {
        if (CommonHelper.isOnline(context)) {
            FileDownloader fileDownloader = new FileDownloader(context, delegate);
            fileDownloader.execute(fileData);
        } else {
            Toast toast = Toast.makeText(context,
                    "Please enable internet connection", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 25, 400);
            toast.show();
        }
    }


    public static GoogleMap getMap(Activity activity) {

        return ((MapFragment) activity.getFragmentManager().
                findFragmentById(com.vanikad.hw.londonlivetraffic.R.id.map)).getMap();
    }

    public static List<CamListXmlParser.Cam> getCameraList(Context context) {
        if (camList == null) {
            setCameraList(context);
        }
        return camList;
    }

    public static void setCameraList(Context context) {
        try {
            camListXmlParser = new CamListXmlParser(context);
            InputStream in = context.openFileInput("myxml.xml");
            camList = camListXmlParser.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

}
