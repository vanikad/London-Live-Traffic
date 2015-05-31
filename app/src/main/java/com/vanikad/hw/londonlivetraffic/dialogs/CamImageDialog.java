package com.vanikad.hw.londonlivetraffic.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.vanikad.hw.londonlivetraffic.AppData;
import com.vanikad.hw.londonlivetraffic.R;
import com.vanikad.hw.londonlivetraffic.database.DictionaryDatabase;
import com.vanikad.hw.londonlivetraffic.network.VolleySingleton;

/**
 * Created by HW on 5/19/2015.
 */
public class CamImageDialog extends DialogFragment {
    Context context;
    TextView messageTextView;
    ImageButton closeButton;
    ImageButton autoRefreshButton;
    ImageButton favButton;
    NetworkImageView networkImageView;
    String url;
    ImageLoader imageLoader;
    SharedPreferences prefs;
    long rowid;
    DictionaryDatabase dictionaryDatabase;
    Cursor allInfoCursor;
    ListFavoritesDialog listFavoritesDialog;

    public CamImageDialog() {
    }

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            Log.e("Timer fired", "Timer fired");
            if (networkImageView != null){
                VolleySingleton.getInstance(context).getRequestQueue().getCache().remove(url);
                networkImageView.setImageUrl(url, imageLoader);
                networkImageView.setImageUrl(null, imageLoader);
                networkImageView.setImageUrl(url, imageLoader);
                Log.e("Image reoladed", "Image Reloaded");
            }
            timerHandler.postDelayed(this, 120000);
        }
    };


    @Override
    public void setArguments(Bundle args) {
        rowid = args.getLong(AppData.ROWID);
    }

    public void setContext(Context context){
        this.context = context;
        dictionaryDatabase = new DictionaryDatabase(context);
        String[] allInfoString = {DictionaryDatabase.KEY_LOCATION, DictionaryDatabase.KEY_URL};
        allInfoCursor = dictionaryDatabase.getWord(String.valueOf(rowid), allInfoString);
        url = allInfoCursor.getString(allInfoCursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_URL));
    }

    public void setListFavoritesDialog(ListFavoritesDialog listFavoritesDialog){
        this.listFavoritesDialog = listFavoritesDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(context == null) {
            Log.d("Context is null", "Dialog dismissed");
            return null;
        }
        prefs = context.getSharedPreferences(AppData.PREFERENCES, Context.MODE_PRIVATE);
        View layout = inflater.inflate(R.layout.cam_image_dialog, container);

        favButton = (ImageButton)layout.findViewById(R.id.favoriteDialogButton);
        setFavButton();
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFav();
                setFavButton();
            }
        });

        closeButton = (ImageButton) layout.findViewById(R.id.imageDialogCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               dismiss();
                                           }
                                       }
        );
        autoRefreshButton = (ImageButton)layout.findViewById(R.id.autoRefreshDialogButton);
        if (isAutoRefreshEnabled()){
            autoRefreshButton.setImageResource(R.drawable.hourglass_color);
            startTimer();
        } else {
            autoRefreshButton.setImageResource(R.drawable.hourglass);
        }
        autoRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAutoRefreshEnabled()){
                    setAutoRefreshEnabled(false);
                    autoRefreshButton.setImageResource(R.drawable.hourglass);
                    Toast.makeText(context, "Auto refresh set to OFF", Toast.LENGTH_LONG).show();
                    stopTimer();

                } else {
                    setAutoRefreshEnabled(true);
                    autoRefreshButton.setImageResource(R.drawable.hourglass_color);
                    Toast.makeText(context, "Auto refresh set to ON", Toast.LENGTH_LONG).show();
                    startTimer();
                }
            }
        });

        messageTextView = (TextView)layout.findViewById(R.id.messageLocation);
        messageTextView.setText(allInfoCursor.getString(allInfoCursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_LOCATION)));
        networkImageView = (NetworkImageView)layout.findViewById(R.id.camImage);
        imageLoader = VolleySingleton.getInstance(context.getApplicationContext()).getImageLoader();

        networkImageView.setErrorImageResId(com.vanikad.hw.londonlivetraffic.R.drawable.networkerror);
        networkImageView.setDefaultImageResId(com.vanikad.hw.londonlivetraffic.R.drawable.download);
        networkImageView.setImageUrl((url), imageLoader);

        return layout;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("Dialog dismissed", "Dialog dismissed");
        stopTimer();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public void startTimer(){
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 60000);
        Log.e("Timer started","Timer started");
    }

    public void stopTimer(){
        timerHandler.removeCallbacks(timerRunnable);
        Log.e("Timer stopped","Timer stopped");
    }

    public boolean isAutoRefreshEnabled(){
        return prefs.getBoolean(AppData.PREFS_AUTO_REFRESH, true);
    }

    public void setAutoRefreshEnabled(boolean state){
        if (state == true){
            prefs.edit().putBoolean(AppData.PREFS_AUTO_REFRESH, true).commit();
        } else {
            prefs.edit().putBoolean(AppData.PREFS_AUTO_REFRESH, false).commit();
        }
    }

    public void setFavButton(){
        String favorite = "";
        String[] columns = {DictionaryDatabase.KEY_FAVOURITE};
        Cursor cursor = dictionaryDatabase.getWord(String.valueOf(rowid), columns);

        if (cursor != null) {
            Log.d("Cursor not null", "");
            cursor.moveToFirst();
            int favIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_FAVOURITE);
            favorite = cursor.getString(favIndex);
            cursor.close();
        }
        Log.d("FAV", favorite);
        if (!favorite.equals("")){
            favButton.setImageResource(R.drawable.favorite_color);
        } else favButton.setImageResource(R.drawable.favorite);
    }

    public void switchFav(){
        if (getFavState()){
            dictionaryDatabase.updateWord(String.valueOf(rowid), "");
            Toast.makeText(context, "Removed from favourites", Toast.LENGTH_LONG).show();
        } else {
            dictionaryDatabase.updateWord(String.valueOf(rowid), AppData.FAVOURITE);
            Toast.makeText(context, "Added to favourites", Toast.LENGTH_LONG).show();
        }
        if (listFavoritesDialog != null) {listFavoritesDialog.restartLoader();}
    }

    public boolean getFavState(){
        String favorite = "";
        String[] columns = {DictionaryDatabase.KEY_FAVOURITE};
        Cursor cursor = dictionaryDatabase.getWord(String.valueOf(rowid), columns);

        if (cursor != null) {
            cursor.moveToFirst();
            int favIndex = cursor.getColumnIndexOrThrow(DictionaryDatabase.KEY_FAVOURITE);
            favorite = cursor.getString(favIndex);
        }

        if (!favorite.equals("")){
            return true;
        } else return false;
    }


}
