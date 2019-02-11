package com.newplanindustries.paneluniformitytester;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SETTINGS_PERMISSION = 7893;
    private static final int STEP_DISTANCE = 20;

    private Activity activity;

    @BindView(R.id.filled)
    ImageView filled;

    GradientDrawable filledShape;

    @BindView(R.id.instructions)
    View instructions;

    @BindView(R.id.brightnessContainer)
    View brightnessContainer;

    @BindView(R.id.brightnessDisplay)
    TextView brightnessDisplay;

    MyColorPicker colorPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Store a reference to the activity
        activity = this;

        //Set the view of the activity
        setContentView(R.layout.activity_main);

        //Bind the the view of the activity to class fields
        ButterKnife.bind(activity);

        //Check if the app has permission to adjust brightness
        if (!checkPermissions()) {
            //Request permission if we don't have it
            requestPermission();
        }

        //Set the main listener
        filled.setOnTouchListener(filledListener);

        //Get the shape from the image
        filledShape = (GradientDrawable) filled.getDrawable();

        //Set the current brightness value into the display
        brightnessDisplay.setText(String.valueOf(getBrightness()));

        //Create the color picker
        colorPicker = new MyColorPicker(activity, 255, 255, 255, 255);

        //Set the callback for the color picker
        colorPicker.setCallback(colorPickerCallback);
    }

    private int getBrightness() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void setBrightness(int brightness) {
        //Check to see if we are even allowed
        if (!checkPermissions()) {
            return;
        }
        //Ensure the brightness value is between 0 and 100
        brightness = Math.min(100, Math.max(brightness, 0));

        //Set the new brightness to the system
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);

        //Let the window know the brightness has changed
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.screenBrightness = brightness / 255f;
        getWindow().setAttributes(attributes);

        //Update the indicator to display the new value
        brightnessDisplay.setText(String.valueOf(brightness));
    }

    View.OnTouchListener filledListener = new View.OnTouchListener() {
        private int initialBrightness;
        private float initialTouchY;
        private long lastTapTime = 0;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //Get the current brightness of the screen
                initialBrightness = getBrightness();

                //Get the position of the touch on the screen
                initialTouchY = motionEvent.getRawY();

                //Make the brightness indicator visible
                brightnessContainer.setVisibility(View.VISIBLE);

                //Hide the instructions
                instructions.setVisibility(View.GONE);

                long currentTapTime = System.currentTimeMillis();
                if (currentTapTime - lastTapTime < ViewConfiguration.getDoubleTapTimeout()) {
                    colorPicker.show();
                }
                lastTapTime = currentTapTime;

                //Return that the touch was handled
                return true;
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                //Hide the brightness indicator
                brightnessContainer.setVisibility(View.GONE);

                //Return that the touch was handled
                return true;
            }
            else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                //Calculate the change in brightness
                int brightnessChange = Math.round(((initialTouchY - motionEvent.getRawY()) / STEP_DISTANCE));

                //Set the new brightness level
                setBrightness(initialBrightness + brightnessChange);

                //Return that the touch was handled
                return true;
            }

            //Return that the touch was not handled
            return false;
        }
    };

    ColorPickerCallback colorPickerCallback = new ColorPickerCallback() {
        @Override
        public void onColorChosen(@ColorInt int color) {
            //Set the color of the drawable
            filledShape.setColor(color);

            //Hide the color picker
            colorPicker.hide();
        }
    };

    private boolean checkPermissions() {
        //Check if the API we are running on even needs to be checked
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        //Check if we already have permission to draw over the screen
        if (Settings.System.canWrite(activity)) {
            return true;
        }

        //Return false because we didn't have permission
        return false;
    }

    private void requestPermission() {
        //Initialize the dialog builder
        (new AlertDialog.Builder(activity))

            //Set the title
            .setTitle(R.string.request_system_permission_title)

            //Set the message
            .setMessage(R.string.request_system_permission)

            //Set the OK button action
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //Create the intent for granting permission
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + activity.getPackageName()));

                    //Start the activity for a result
                    activity.startActivityForResult(intent, REQUEST_SETTINGS_PERMISSION);

                    //Instruct the user what they need to do
                    Toast.makeText(activity, R.string.grant_permission_go_back, Toast.LENGTH_LONG).show();
                }
            })

            //Build and show the dialog
            .create().show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Check if we even have focus
        if (!hasFocus) {
            return;
        }

        //If we have focus, hide the system UI
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
