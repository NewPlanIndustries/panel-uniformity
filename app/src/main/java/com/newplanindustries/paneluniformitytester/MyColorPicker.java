package com.newplanindustries.paneluniformitytester;

import android.app.Activity;
import android.os.Bundle;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

public class MyColorPicker extends ColorPicker {
    public MyColorPicker(Activity activity, int alpha, int red, int green, int blue) {
        super(activity, alpha, red, green, blue);
    }

    boolean contentHasBeenSet = false;

    @Override
    public void setContentView(int layoutResID) {
        if (contentHasBeenSet == false) {
            super.setContentView(layoutResID);
            contentHasBeenSet = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.color_picker_dialog);
        super.onCreate(savedInstanceState);
    }
}
