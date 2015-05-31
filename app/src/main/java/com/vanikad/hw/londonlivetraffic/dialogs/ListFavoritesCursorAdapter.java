package com.vanikad.hw.londonlivetraffic.dialogs;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vanikad.hw.londonlivetraffic.AppData;
import com.vanikad.hw.londonlivetraffic.R;
import com.vanikad.hw.londonlivetraffic.database.DictionaryDatabase;

/**
 * Created by HW on 5/29/2015.
 */
public class ListFavoritesCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;
    CamImageDialog camImageDialog;
    FragmentManager fm;
    Context context;
    ListFavoritesDialog listFavoritesDialog;
    DictionaryDatabase dictionaryDatabase;

    // Constructor
    public ListFavoritesCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.dictionaryDatabase = new DictionaryDatabase(context);
    }

    public void setListFavoritesDialog(ListFavoritesDialog listFavoritesDialog){
        this.listFavoritesDialog = listFavoritesDialog;
    }
    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.fav_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView locationTextView = (TextView) view.findViewById(R.id.favLocationListItem);
        final ImageButton removeItemButton = (ImageButton) view.findViewById(R.id.removeItemButton);
        String location = cursor.getString(cursor.getColumnIndex(DictionaryDatabase.KEY_LOCATION));
        locationTextView.setText(location);

        int rowidIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        final long rowid = cursor.getInt(rowidIndex);

        removeItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(rowid);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Textview clicked", "Textview clicked");
                showPhotoDialog(context, rowid);
            }
        });

    }

    public void showPhotoDialog(Context context, long rowid) {
        camImageDialog = new CamImageDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(AppData.ROWID, rowid);
        camImageDialog.setArguments(bundle);
        camImageDialog.setContext(context);
        camImageDialog.setListFavoritesDialog(listFavoritesDialog);
        camImageDialog.setRetainInstance(true);
        camImageDialog.show(fm, "camImageDialog");
    }


    private void removeItem(long rowid){
        dictionaryDatabase.updateWord(String.valueOf(rowid), "");
        listFavoritesDialog.restartLoader();

    }

}
