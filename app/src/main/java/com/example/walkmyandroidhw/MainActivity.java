package com.example.walkmyandroidhw;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/*
 * Answers to homework questions
 *
 * Q1 - Which API do you use to request the last known location on the device?
 * A1 - getLastLocation() method in the FusedLocationProviderClient class
 *
 * Q2 - Which class do you use for handling geocoding and reverse geocoding?
 * A2 - Geocoder
 *
 * Q3 - Which method do you use for periodic location updates ?
 * A3 - requestLocationUpdates() method in the FusedLocationProviderClient class
 *
 * */
public class MainActivity extends AppCompatActivity implements FetchAddressTask.OnTaskCompleted {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TRACKING_LOCATION_KEY = "tracking_location_key";
    private TextView mLocationTextView;
    private Button mButton;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;
    private boolean mTrackingLocation;
    private LocationCallback mLocationCallback;
    private Location currentLocation;
    private TextView mSpeedAccuracyTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button_location);
        mLocationTextView = findViewById(R.id.textview_location);
        mAndroidImageView = findViewById(R.id.imageview_android);
        mSpeedAccuracyTextView = findViewById(R.id.speed_accuracy_textView);
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });
        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation) {
                    currentLocation = locationResult.getLastLocation();
                    new FetchAddressTask(MainActivity.this, MainActivity.this).execute(currentLocation);
                }
            }
        };
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
        }
        mLocationTextView.setText(getString(R.string.address_text, getString(R.string.loading), System.currentTimeMillis()));
        mRotateAnim.start();
        mTrackingLocation = true;
        mButton.setText(R.string.stop_tracking_location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTrackingLocation();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mButton.setText(R.string.get_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            mSpeedAccuracyTextView.setText("");
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onTaskCompleted(String result) {
        if (mTrackingLocation) {
            mLocationTextView.setText(getString(R.string.address_text, result, System.currentTimeMillis()));
            float speed, accuracy;
            speed = currentLocation.getSpeed();
            accuracy = currentLocation.getAccuracy();
            mSpeedAccuracyTextView.setText(getString(R.string.speed_accuracy_text, speed, accuracy));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTrackingLocation){
            startTrackingLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTrackingLocation){
            stopTrackingLocation();
            mTrackingLocation = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }
}
