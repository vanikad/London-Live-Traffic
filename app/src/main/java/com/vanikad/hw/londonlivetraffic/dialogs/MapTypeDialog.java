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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.GoogleMap;
import com.vanikad.hw.londonlivetraffic.R;

/**
 * Created by HW on 5/13/2015.
 */
public class MapTypeDialog extends DialogFragment {


    Context context;
    RadioGroup radioGroup;
    RadioButton radioButtonMapNormal;
    RadioButton radioButtonMapHybrid;
    RadioButton radioButtonMapSatellite;
    RadioButton radioButtonMapTerrain;
    ImageButton okButton;
    GoogleMap map;

    public MapTypeDialog() {
    }

    public void setArguments(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
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
        View view = inflater.inflate(R.layout.map_type_dialog, container);
        radioGroup = (RadioGroup) view.findViewById(R.id.mapTypeRadioGroup);
        radioButtonMapNormal = (RadioButton) view.findViewById(R.id.mapTypeNormal);
        radioButtonMapHybrid = (RadioButton) view.findViewById(R.id.mapTypeHybrid);
        radioButtonMapSatellite = (RadioButton) view.findViewById(R.id.mapTypeSatellite);
        radioButtonMapTerrain = (RadioButton) view.findViewById(R.id.mapTypeTerrain);
        setCheckedButton();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.mapTypeNormal:
                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.mapTypeHybrid:
                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case R.id.mapTypeSatellite:
                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case R.id.mapTypeTerrain:
                        map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    default:
                        ;
                        break;
                }
            }
        });
        okButton = (ImageButton) view.findViewById(R.id.mapTypeOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dismiss();
                                        }
                                    }
        );
        return view;
    }

    private void setCheckedButton(){
        switch (map.getMapType()) {
            case GoogleMap.MAP_TYPE_NORMAL:
                radioButtonMapNormal.setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                radioButtonMapHybrid.setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_SATELLITE:
                radioButtonMapSatellite.setChecked(true);
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                radioButtonMapTerrain.setChecked(true);
                break;
            default:
                ;
                break;
        }
    }

}
