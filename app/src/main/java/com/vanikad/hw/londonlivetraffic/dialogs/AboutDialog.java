package com.vanikad.hw.londonlivetraffic.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vanikad.hw.londonlivetraffic.R;

/**
 * Created by HW on 5/13/2015.
 */
public class AboutDialog extends DialogFragment {


    Context context;
    TextView aboutText;
    ImageButton closeButton;

    public AboutDialog() {
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
        View view = inflater.inflate(R.layout.about_dialog, container);
        closeButton = (ImageButton) view.findViewById(R.id.aboutCloseButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dismiss();
                                        }
                                    }
        );
        return view;
    }



}
