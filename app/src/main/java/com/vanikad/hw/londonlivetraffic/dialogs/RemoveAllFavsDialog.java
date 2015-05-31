package com.vanikad.hw.londonlivetraffic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.vanikad.hw.londonlivetraffic.R;

/**
 * Created by HW on 5/30/2015.
 */
public class RemoveAllFavsDialog extends DialogFragment {

    ListFavoritesDialog listFavoritesDialog;

    public void setListFavoritesDialog(ListFavoritesDialog listFavoritesDialog){
        this.listFavoritesDialog = listFavoritesDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.warning)
                .setTitle("Remove favourites")
                .setMessage("All favourites will be removed. Please, confirm or cancel.")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                listFavoritesDialog.removeAllFavorites();
                                getDialog().dismiss();
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getDialog().dismiss();
                            }
                        }
                )
                .create();
    }
}