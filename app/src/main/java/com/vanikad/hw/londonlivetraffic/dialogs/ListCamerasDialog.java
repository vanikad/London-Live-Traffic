package com.vanikad.hw.londonlivetraffic.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.vanikad.hw.londonlivetraffic.R;
import com.vanikad.hw.londonlivetraffic.database.DictionaryDatabase;
import com.vanikad.hw.londonlivetraffic.database.DictionaryProvider;

/**
 * Created by HW on 5/19/2015.
 */
public class ListCamerasDialog extends DialogFragment implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{
    Context context;
    DictionaryDatabase dictionaryDatabase;
    View layout;
    ListView listView;
    SimpleCursorAdapter dataAdapter;
    ListCamerasCursorAdapter mCamerasAdapter;
    CursorLoader cursorLoader;

    ImageButton closeButton;


    public ListCamerasDialog() {
    }

    @Override
    public void setArguments(Bundle args) {
    }

    public void setContext(Context context){
        this.context = context;
        dictionaryDatabase = new DictionaryDatabase(context);
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
        layout = inflater.inflate(R.layout.list_cameras_dialog, container);
        displayFavList();
        closeButton = (ImageButton)layout.findViewById(R.id.camDialogCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return layout;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d("Dialog dismissed", "Dialog dismissed");
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private void displayFavList(){

        // The desired columns to be bound
        String[] columns = new String[] {
                DictionaryDatabase.KEY_LOCATION
        };

        int[] to = new int[] {
                R.id.favLocationListItem
        };

        mCamerasAdapter = new ListCamerasCursorAdapter(context, null, 0);
        mCamerasAdapter.setFragmentManager(getFragmentManager());

        // get reference to the ListView
        listView = (ListView)layout.findViewById(R.id.camerasListView);
        TextView emptyText = (TextView)layout.findViewById(android.R.id.empty);
        listView.setEmptyView(emptyText);

//        // Assign adapter to ListView
        listView.setAdapter(mCamerasAdapter);
//        //Ensures a loader is initialized and active.
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                BaseColumns._ID,
                DictionaryDatabase.KEY_LOCATION};
        cursorLoader = new CursorLoader(context,
                DictionaryProvider.CONTENT_URI_ALL, projection, null, null, DictionaryDatabase.KEY_LOCATION + " ASC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mCamerasAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mCamerasAdapter.swapCursor(null);
    }
}
