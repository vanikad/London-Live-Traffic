package com.vanikad.hw.londonlivetraffic.dialogs;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vanikad.hw.londonlivetraffic.AppData;
import com.vanikad.hw.londonlivetraffic.R;
import com.vanikad.hw.londonlivetraffic.database.DictionaryDatabase;

/**
 * Created by HW on 5/29/2015.
 */
public class ListCamerasCursorAdapter extends CursorAdapter {

    private LayoutInflater cursorInflater;
    CamImageDialog camImageDialog;
    FragmentManager fm;

    // Constructor
    public ListCamerasCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return cursorInflater.inflate(R.layout.cameras_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView locationTextView = (TextView) view.findViewById(R.id.camListItemLocation);
        String location = cursor.getString(cursor.getColumnIndex(DictionaryDatabase.KEY_LOCATION));
        locationTextView.setText(location);

        int rowidIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        final long rowid = cursor.getInt(rowidIndex);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhotoDialog(context, rowid);
            }
        });
    }

    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
    }


    public void showPhotoDialog(Context context, long rowid) {
        camImageDialog = new CamImageDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(AppData.ROWID,rowid);
        camImageDialog.setArguments(bundle);
        camImageDialog.setContext(context);
        camImageDialog.setRetainInstance(true);
        camImageDialog.show(fm, "camImageDialog");
    }

}
